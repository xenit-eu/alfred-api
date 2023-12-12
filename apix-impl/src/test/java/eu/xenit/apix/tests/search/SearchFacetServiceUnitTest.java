package eu.xenit.apix.tests.search;

import eu.xenit.apix.alfresco.search.SearchFacetsService;
import eu.xenit.apix.alfresco.search.SearchFacetsServiceImpl;
import eu.xenit.apix.search.FacetSearchResult;
import eu.xenit.apix.search.FacetSearchResult.FacetValue;
import eu.xenit.apix.search.SearchQuery.FacetOptions;
import eu.xenit.apix.translation.ITranslationService;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetHelper;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabelDisplayHandlerRegistry;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchFacetServiceUnitTest {

    @Autowired
    private SearchFacetsService searchFacetsService;

    FacetOptions facetOptionsMock;
    ResultSet resultSetMock;
    SearchParameters searchParametersMock;

    public void initMocks() {
        ServiceRegistry serviceRegistryMock = mock(ServiceRegistry.class);

        SolrFacetHelper solrFacetHelperMock = mock(SolrFacetHelper.class);

        FacetLabelDisplayHandlerRegistry facetLabelDisplayHandlerRegistryStub =
                new FacetLabelDisplayHandlerRegistry();

        DataTypeDefinition textDataTypeDef = mock(DataTypeDefinition.class);
        when(textDataTypeDef.getName()).thenReturn(DataTypeDefinition.TEXT);

        DictionaryService dictionaryServiceMock = mock(DictionaryService.class);

        QName languageQName = QName.createQName("{http://test.apix.xenit.eu/model/content}language");
        PropertyDefinition languagePropDefMock = mock(PropertyDefinition.class);
        List<ConstraintDefinition> languageConstraintDefinitions = new ArrayList<>();
        ConstraintDefinition theLanguageConstraintDefinition = mock(ConstraintDefinition.class);
        ListOfValuesConstraint theListOfValuesConstraint = mock(ListOfValuesConstraint.class);
        List<String> listOfValues = new ArrayList<String>();
        listOfValues.add("Dutch");
        theListOfValuesConstraint.setAllowedValues(listOfValues);
        theListOfValuesConstraint
                .setShortName("{http://test.apix.xenit.eu/model/content}model_hasLanguage_language_anon_0");
        when(theListOfValuesConstraint.getDisplayLabel(eq("Dutch"), any(MessageLookup.class))).thenReturn("Nederlands");
        when(theLanguageConstraintDefinition.getConstraint()).thenReturn(theListOfValuesConstraint);
        languageConstraintDefinitions.add(theLanguageConstraintDefinition);
        when(languagePropDefMock.getConstraints()).thenReturn(languageConstraintDefinitions);
        when(languagePropDefMock.getDataType()).thenReturn(textDataTypeDef);
        when(dictionaryServiceMock.getProperty(languageQName)).thenReturn(languagePropDefMock);

        QName documentStatusQName = QName.createQName("{http://test.apix.xenit.eu/model/content}documentStatus");
        PropertyDefinition documentStatusPropDefMock = mock(PropertyDefinition.class);
        List<ConstraintDefinition> documnetStatusConDefList = new ArrayList<>();
        ConstraintDefinition documentStatusConDef = mock(ConstraintDefinition.class);
        ListOfValuesConstraint docStatLOVConstr = mock(ListOfValuesConstraint.class);
        List<String> docStatLOV = new ArrayList<>();
        docStatLOV.add("Draft");
        docStatLOVConstr.setAllowedValues(docStatLOV);
        docStatLOVConstr.setShortName(
                "{http://test.apix.xenit.eu/model/content}model_withMandatoryPropDocument_documentStatus_anon_0");
        when(docStatLOVConstr.getDisplayLabel(eq("Draft"), any(MessageLookup.class))).thenReturn("Draft");
        when(documentStatusConDef.getConstraint()).thenReturn(docStatLOVConstr);
        documnetStatusConDefList.add(documentStatusConDef);
        when(documentStatusPropDefMock.getConstraints()).thenReturn(documnetStatusConDefList);
        when(documentStatusPropDefMock.getDataType()).thenReturn(textDataTypeDef);
        when(dictionaryServiceMock.getProperty(documentStatusQName)).thenReturn(documentStatusPropDefMock);

        when(serviceRegistryMock.getDictionaryService()).thenReturn(dictionaryServiceMock);
        when(serviceRegistryMock.getSolrFacetHelper()).thenReturn(solrFacetHelperMock);
        when(serviceRegistryMock.getFacetLabelDisplayHandlerRegistry()).thenReturn(facetLabelDisplayHandlerRegistryStub);
        searchFacetsService = new SearchFacetsServiceImpl(serviceRegistryMock, mock(SolrFacetService.class), mock(
                ITranslationService.class));

        facetOptionsMock = mock(FacetOptions.class);
        when(facetOptionsMock.isEnabled()).thenReturn(true);

        resultSetMock = mock(ResultSet.class);
        List<Pair<String, Integer>> languageFieldFacetResults = new ArrayList<>();
        List<Pair<String, Integer>> documentStatusFieldFacetResults = new ArrayList<>();
        Pair<String, Integer> languageFieldFacetResult = new Pair<String, Integer>("Dutch", 1);
        Pair<String, Integer> documentStatusFieldFacetResult = new Pair<String, Integer>("Draft", 1);
        languageFieldFacetResults.add(languageFieldFacetResult);
        documentStatusFieldFacetResults.add(documentStatusFieldFacetResult);
        when(resultSetMock.getFieldFacet("@{http://test.apix.xenit.eu/model/content}language"))
                .thenReturn(languageFieldFacetResults);
        when(resultSetMock.getFieldFacet("@{http://test.apix.xenit.eu/model/content}documentStatus"))
                .thenReturn(documentStatusFieldFacetResults);
        Map<String, Integer> facetQueries = new HashMap<>();
        facetQueries.put("{!afts}@{http://www.alfresco.org/model/content/1.0}content.size:[0 TO 10240]", 1);
        facetQueries.put("{!afts}@{http://www.alfresco.org/model/content/1.0}modified:[NOW/DAY-1YEAR TO NOW/DAY+1DAY]", 2);
        facetQueries.put("{!afts}@{http://www.alfresco.org/model/content/1.0}created:[2020-08-31T07:00:00.000Z TO 2023-09-02T10:01:00.000Z]", 1);
        when(resultSetMock.getFacetQueries()).thenReturn(facetQueries);

        searchParametersMock = mock(SearchParameters.class);
        List<FieldFacet> fieldFacets = new ArrayList<>();
        FieldFacet fieldFacetMock_A = mock(FieldFacet.class);
        when(fieldFacetMock_A.getField()).thenReturn("@{http://test.apix.xenit.eu/model/content}language");
        fieldFacets.add(fieldFacetMock_A);
        FieldFacet fieldFacetMock_B = mock(FieldFacet.class);
        when(fieldFacetMock_B.getField()).thenReturn("@{http://test.apix.xenit.eu/model/content}documentStatus");
        fieldFacets.add(fieldFacetMock_B);
        when(searchParametersMock.getFieldFacets()).thenReturn(fieldFacets);
    }

    public List<FacetSearchResult> initExpectedResult_for_assertThat_getFacetResults_returnIncludes_translationsForListOfValueConstraints() {
        List<FacetSearchResult> expectedResult = new ArrayList<>();
        FacetSearchResult languageResult = new FacetSearchResult();
        languageResult.setName("{http://test.apix.xenit.eu/model/content}language");
        List<FacetValue> languageFacetValues = new ArrayList<>();
        FacetValue dutchFacetValue = new FacetValue();
        dutchFacetValue.setValue("Dutch");
        dutchFacetValue.setLabel("Nederlands");
        dutchFacetValue.setCount(1);
        languageFacetValues.add(dutchFacetValue);
        languageResult.setValues(languageFacetValues);
        expectedResult.add(languageResult);
        FacetSearchResult documentStatusResult = new FacetSearchResult();
        documentStatusResult.setName("{http://test.apix.xenit.eu/model/content}documentStatus");
        List<FacetValue> documentStatusValues = new ArrayList<>();
        FacetValue draftFacetValue = new FacetValue();
        draftFacetValue.setValue("Draft");
        draftFacetValue.setLabel("Draft");
        draftFacetValue.setCount(1);
        documentStatusValues.add(draftFacetValue);
        documentStatusResult.setValues(documentStatusValues);
        expectedResult.add(documentStatusResult);
        FacetSearchResult contentResult = new FacetSearchResult();
        contentResult.setName("{http://www.alfresco.org/model/content/1.0}content.size");
        List<FacetValue> contentValues = new ArrayList<>();
        FacetValue contentFacetValue = new FacetValue();
        contentFacetValue.setValue("[0 TO 10240]");
        contentFacetValue.setCount(1);
        contentValues.add(contentFacetValue);
        contentResult.setValues(contentValues);
        expectedResult.add(contentResult);
        FacetSearchResult modifiedResult = new FacetSearchResult();
        modifiedResult.setName("{http://www.alfresco.org/model/content/1.0}modified");
        List<FacetValue> modifiedValues = new ArrayList<>();
        FacetValue modifiedFacetValue = new FacetValue();
        modifiedFacetValue.setValue("[NOW/DAY-1YEAR TO NOW/DAY+1DAY]");
        modifiedFacetValue.setCount(2);
        modifiedValues.add(modifiedFacetValue);
        modifiedResult.setValues(modifiedValues);
        expectedResult.add(modifiedResult);
        FacetSearchResult createdResult = new FacetSearchResult();
        createdResult.setName("{http://www.alfresco.org/model/content/1.0}created");
        List<FacetValue> createdValues = new ArrayList<>();
        FacetValue createdFacetValue = new FacetValue();
        createdFacetValue.setValue("[2020-08-31T07:00:00.000Z TO 2023-09-02T10:01:00.000Z]");
        createdFacetValue.setCount(1);
        createdValues.add(createdFacetValue);
        createdResult.setValues(createdValues);
        expectedResult.add(createdResult);
        return expectedResult;
    }

    @Test
    public void assertThat_getFacetResults_returnIncludes_translationsForListOfValueConstraints() {
        initMocks();
        List<FacetSearchResult> expectedResult = initExpectedResult_for_assertThat_getFacetResults_returnIncludes_translationsForListOfValueConstraints();
        List<FacetSearchResult> result = searchFacetsService.getFacetResults(facetOptionsMock, resultSetMock,
                searchParametersMock);
        assertEquals(expectedResult, result);
    }

}
