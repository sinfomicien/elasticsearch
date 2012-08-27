package org.elasticsearch.grouped;

public class GroupedFacetHitEntry {
    private final String facetValue;
    private final String groupValue;

    GroupedFacetHitEntry(String facetValue, String groupValue) {
        this.facetValue = facetValue;
        this.groupValue = groupValue;
    }

	public String getFacetValue() {
		return facetValue;
	}

	public String getGroupValue() {
		return groupValue;
	}
}
