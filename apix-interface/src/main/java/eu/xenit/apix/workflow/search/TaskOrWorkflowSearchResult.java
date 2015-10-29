package eu.xenit.apix.workflow.search;

import eu.xenit.apix.workflow.model.ITaskOrWorkflow;

import java.util.List;

public class TaskOrWorkflowSearchResult {

    /**
     * In case the user does not request the details of the results (only the ids e.g.), this should be null, not an
     * empty list.
     */
    public List<ITaskOrWorkflow> results;
    public List<String> IDs;
    public int nbResults; //Number of results if no paging would have been applied.
    private Facets facets;

    public TaskOrWorkflowSearchResult(List<ITaskOrWorkflow> results, List<String> IDs, Facets facets, int nbResults) {
        this.results = results;
        this.IDs = IDs;
        this.facets = facets;
        this.nbResults = nbResults;
    }

    public TaskOrWorkflowSearchResult() {
    }

    public List<ITaskOrWorkflow> getResults() {
        return results;
    }

    public void setResults(List<ITaskOrWorkflow> results) {
        this.results = results;
    }


    public List<String> getIDs() {
        return IDs;
    }

    public void setIDs(List<String> IDs) {
        this.IDs = IDs;
    }


    public int getNbResults() {
        return nbResults;
    }

    public void setNbResults(int nbResults) {
        this.nbResults = nbResults;
    }

    public Facets getFacets() {
        return facets;
    }

    public void setFacets(Facets facets) {
        this.facets = facets;
    }
}