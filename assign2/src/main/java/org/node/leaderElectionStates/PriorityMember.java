package org.node.leaderElectionStates;

import java.util.Objects;

public class PriorityMember implements Comparable<PriorityMember> {
    private String hash;
    private int countersSum;

    public PriorityMember(String hash, int countersSum) {
        this.hash = hash;
        this.countersSum = countersSum;
    }

    public String getHash() {
        return hash;
    }

    public int getCountersSum() {
        return countersSum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PriorityMember that = (PriorityMember) o;
        return countersSum == that.countersSum && Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, countersSum);
    }

    @Override
    public int compareTo(PriorityMember o) {
        if (this.countersSum > o.countersSum) {
            // if current object is greater,then return 1
            return -1;
        }
        else if (this.countersSum < o.countersSum) {
            // if current object is greater,then return -1
            return 1;
        }
        else {
            // if current object is equal to o compare hashes
            return this.hash.compareTo(o.hash);
        }
    }

    public void setCountersSum(int counter) {
        countersSum = counter;
    }

    public String toString(){
        return "Hash: " + hash + " counter: " + countersSum;
    }
}
