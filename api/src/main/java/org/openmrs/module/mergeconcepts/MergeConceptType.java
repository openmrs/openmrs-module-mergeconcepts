package org.openmrs.module.mergeconcepts;

public enum MergeConceptType {
    OLD, NEW;

    @Override
    public String toString() {
        String conceptTypeString = super.toString();
        return conceptTypeString.toLowerCase();
    }
}
