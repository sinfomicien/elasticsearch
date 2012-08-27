package org.elasticsearch.grouped;

import org.elasticsearch.common.CacheRecycler;
import org.elasticsearch.index.field.data.FieldData;
import org.elasticsearch.index.field.data.strings.StringFieldData;

abstract public class ReaderAggregator implements FieldData.OrdinalInDocProc {
    final String[] values;
    final int[] counts;

    int position = 0;
    String current;
    int total;

    public ReaderAggregator(StringFieldData fieldData) {
        this.values = fieldData.values();
        this.counts = CacheRecycler.popIntArray(fieldData.values().length);
    }

    public boolean nextPosition() {
        if (++position >= values.length) {
            return false;
        }
        current = values[position];
        return true;
    }
}
