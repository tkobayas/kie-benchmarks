package org.drools.benchmarks.model;

import org.drools.core.util.index.Interval;

public class LongInterval implements Interval<Long> {

    private String id;

    private Long start;
    private boolean startInclusive;

    private Long end;
    private boolean endInclusive;

    public LongInterval(String id, Long start, boolean startInclusive, Long end, boolean endInclusive) {
        super();
        this.id = id;
        this.start = start;
        this.startInclusive = startInclusive;
        this.end = end;
        this.endInclusive = endInclusive;
    }

    @Override
    public Long getStart() {
        return start;
    }

    @Override
    public boolean isStartInclusive() {
        return startInclusive;
    }

    @Override
    public Long getEnd() {
        return end;
    }

    @Override
    public boolean isEndInclusive() {
        return endInclusive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LongInterval other = (LongInterval) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LongInterval [id=" + id + ", start=" + start + ", startInclusive=" + startInclusive + ", end=" + end + ", endInclusive=" + endInclusive + "]";
    }

}