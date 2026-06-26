package com.example.demo.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FuzzyQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Like;
import co.elastic.clients.elasticsearch._types.query_dsl.LikeDocument;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.demo.search.dto.SearchRequest;
import com.example.demo.search.dto.SearchResult;
import com.example.demo.search.index.LocationDoc;
import com.example.demo.search.index.LocationIndexInit;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationSearchService {

	private final ElasticsearchClient client;

	public LocationSearchService(ElasticsearchClient client) {
		this.client = client;
	}

	public List<SearchResult> search(SearchRequest req) throws Exception {
		BoolQuery.Builder bool = new BoolQuery.Builder();

		List<Query> textQueries = new ArrayList<>();
		addIfNotNull(textQueries, buildFieldQuery("name", req.getName()));
		addIfNotNull(textQueries, buildFieldQuery("descriptionUi", req.getDescriptionUi()));
		addIfNotNull(textQueries, buildFieldQuery("descriptionPdf", req.getDescriptionPdf()));

		boolean isOr = "OR".equalsIgnoreCase(req.getOperator());
		if (isOr) {
			textQueries.forEach(bool::should);
			if (!textQueries.isEmpty()) bool.minimumShouldMatch("1");
		} else {
			textQueries.forEach(bool::must);
		}

		// range query-ji su uvek MUST (donja/gornja granica opciono)
		addIfNotNull(bool, intRange("reviewCount", req.getReviewCountFrom(), req.getReviewCountTo()));
		addIfNotNull(bool, doubleRange("avgPerformance", req.getAvgPerformanceFrom(), req.getAvgPerformanceTo()));
		addIfNotNull(bool, doubleRange("avgSoundLight", req.getAvgSoundLightFrom(), req.getAvgSoundLightTo()));
		addIfNotNull(bool, doubleRange("avgVenue", req.getAvgVenueFrom(), req.getAvgVenueTo()));
		addIfNotNull(bool, doubleRange("avgOverall", req.getAvgOverallFrom(), req.getAvgOverallTo()));
		addIfNotNull(bool, doubleRange("avgTotal", req.getAvgTotalFrom(), req.getAvgTotalTo()));

		// ako nijedno polje nije zadato, vrati sve (match_all)
		Query finalQuery;
		BoolQuery built = bool.build();
		if (built.must().isEmpty() && built.should().isEmpty() && built.filter().isEmpty()) {
			finalQuery = Query.of(q -> q.matchAll(m -> m));
		} else {
			finalQuery = Query.of(q -> q.bool(built));
		}

		SortOrder order = "desc".equalsIgnoreCase(req.getSortOrder()) ? SortOrder.Desc : SortOrder.Asc;

		SearchResponse<LocationDoc> resp = client.search(s -> s
				.index(LocationIndexInit.INDEX)
				.query(finalQuery)
				.sort(so -> so.field(f -> f.field("name.keyword").order(order)))
				.highlight(h -> h
						.fields("name", hf -> hf)
						.fields("descriptionUi", hf -> hf)
						.fields("descriptionPdf", hf -> hf)
				)
				.size(100),
				LocationDoc.class);

		return mapHits(resp);
	}

	public List<SearchResult> moreLikeThis(String locationName) throws Exception {
		Query mlt = MoreLikeThisQuery.of(b -> b
				.fields("name", "descriptionUi", "descriptionPdf")
				.like(Like.of(l -> l.document(LikeDocument.of(ld -> ld
						.index(LocationIndexInit.INDEX)
						.id(locationName)
				))))
				.minTermFreq(1)
				.maxQueryTerms(25)
				.minDocFreq(1)
		)._toQuery();

		SearchResponse<LocationDoc> resp = client.search(s -> s
				.index(LocationIndexInit.INDEX)
				.query(mlt)
				.highlight(h -> h
						.fields("name", hf -> hf)
						.fields("descriptionUi", hf -> hf)
						.fields("descriptionPdf", hf -> hf)
				)
				.size(20),
				LocationDoc.class);

		return mapHits(resp);
	}

	// PhraseQuery / PrefixQuery / FuzzyQuery / MatchQuery
	private Query buildFieldQuery(String field, String raw) {
		if (raw == null || raw.isBlank()) return null;
		String value = raw.trim();

		// PhraseQuery: "fraza"
		if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
			String phrase = value.substring(1, value.length() - 1);
			return MatchPhraseQuery.of(b -> b.field(field).query(phrase))._toQuery();
		}

		// FuzzyQuery: ~rec
		if (value.startsWith("~") && value.length() > 1) {
			String term = value.substring(1).toLowerCase();
			return FuzzyQuery.of(b -> b.field(field).value(term).fuzziness("AUTO"))._toQuery();
		}

		// PrefixQuery / Wildcard: rec*
		if (value.contains("*")) {
			String wildcard = value.toLowerCase();
			return WildcardQuery.of(b -> b.field(field).value(wildcard))._toQuery();
		}

		// default: MatchQuery (analyzer obradjuje case/cirilicu/dijakritike)
		return MatchQuery.of(b -> b.field(field).query(value))._toQuery();
	}

	private Query intRange(String field, Integer from, Integer to) {
		if (from == null && to == null) return null;
		return Query.of(q -> q.range(r -> r.number(n -> {
			n.field(field);
			if (from != null) n.gte(from.doubleValue());
			if (to != null) n.lte(to.doubleValue());
			return n;
		})));
	}

	private Query doubleRange(String field, Double from, Double to) {
		if (from == null && to == null) return null;
		return Query.of(q -> q.range(r -> r.number(n -> {
			n.field(field);
			if (from != null) n.gte(from);
			if (to != null) n.lte(to);
			return n;
		})));
	}

	private void addIfNotNull(List<Query> list, Query q) {
		if (q != null) list.add(q);
	}

	private void addIfNotNull(BoolQuery.Builder bool, Query q) {
		if (q != null) bool.must(q);
	}

	private List<SearchResult> mapHits(SearchResponse<LocationDoc> resp) {
		List<SearchResult> out = new ArrayList<>();
		for (Hit<LocationDoc> hit : resp.hits().hits()) {
			LocationDoc doc = hit.source();
			if (doc == null) continue;
			SearchResult r = new SearchResult();
			r.setName(doc.getName());
			r.setDescriptionUi(doc.getDescriptionUi());
			r.setReviewCount(doc.getReviewCount());
			r.setAvgTotal(doc.getAvgTotal());
			r.setPdfKey(doc.getPdfKey());
			if (hit.highlight() != null && !hit.highlight().isEmpty()) {
				r.setHighlights(hit.highlight());
			}
			out.add(r);
		}
		return out;
	}
}
