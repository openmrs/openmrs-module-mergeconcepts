package org.openmrs.module.mergeconcepts;

public enum ConceptType {
    OLD, NEW;

    @Override
    public String toString() {
        String conceptTypeString = super.toString();
        return conceptTypeString.toLowerCase();
    }
}
