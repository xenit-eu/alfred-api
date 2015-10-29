package eu.xenit.apix.tests.helperClasses.alfresco.services;

import java.util.Collection;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.search.CMISQueryService;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.forms.FormService;
import org.alfresco.repo.imap.ImapService;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetHelper;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabelDisplayHandlerRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.blog.BlogService;
import org.alfresco.service.cmr.calendar.CalendarService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.EditionService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PublicServiceAccessService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.webdav.WebDavService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

public class AlfrescoServiceRegistryStub implements ServiceRegistry {

    public AlfrescoNodeServiceStub nodeServiceStub;
    public AlfrescoContentServiceStub contentServiceStub;
    public AlfrescoMimetypeServiceStub mimetypeServiceStub;

    public AlfrescoServiceRegistryStub() {
        nodeServiceStub = new AlfrescoNodeServiceStub(null, null);
        contentServiceStub = new AlfrescoContentServiceStub();
        mimetypeServiceStub = new AlfrescoMimetypeServiceStub();
    }

    @Override
    public Collection<QName> getServices() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isServiceProvided(QName service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getService(QName service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DescriptorService getDescriptorService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionService getTransactionService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RetryingTransactionHelper getRetryingTransactionHelper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NamespaceService getNamespaceService() {
        return null;
    }

    @Override
    public MutableAuthenticationService getAuthenticationService() {
        return null;
    }

    @Override
    public NodeService getNodeService() {
        return nodeServiceStub;
    }

    @Override
    public ContentService getContentService() {
        return contentServiceStub;
    }

    @Override
    public MimetypeService getMimetypeService() {
        return mimetypeServiceStub;
    }

    @Override
    public ContentFilterLanguagesService getContentFilterLanguagesService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SearchService getSearchService() {
        return null;
    }

    @Override
    public VersionService getVersionService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockService getLockService() {
        return null;
    }

    @Override
    public JobLockService getJobLockService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DictionaryService getDictionaryService() {
        return null;
    }

    @Override
    public CopyService getCopyService() {
        return null;
    }

    @Override
    public CheckOutCheckInService getCheckOutCheckInService() {
        return null;
    }

    @Override
    public CategoryService getCategoryService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImporterService getImporterService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExporterService getExporterService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RuleService getRuleService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ActionService getActionService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PermissionService getPermissionService() {
        return null;
    }

    @Override
    public AuthorityService getAuthorityService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TemplateService getTemplateService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileFolderService getFileFolderService() {
        return null;
    }

    @Override
    public ScriptService getScriptService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WorkflowService getWorkflowService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NotificationService getNotificationService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuditService getAuditService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OwnableService getOwnableService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PersonService getPersonService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SiteService getSiteService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AttributeService getAttributeService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultilingualContentService getMultilingualContentService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EditionService getEditionService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ThumbnailService getThumbnailService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaggingService getTaggingService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormService getFormService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenditionService getRenditionService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RatingService getRatingService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeLocatorService getNodeLocatorService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlogService getBlogService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CalendarService getCalendarService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InvitationService getInvitationService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CMISDictionaryService getCMISDictionaryService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CMISQueryService getCMISQueryService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImapService getImapService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PublicServiceAccessService getPublicServiceAccessService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RepoAdminService getRepoAdminService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SysAdminParams getSysAdminParams() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebDavService getWebDavService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SolrFacetHelper getSolrFacetHelper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FacetLabelDisplayHandlerRegistry getFacetLabelDisplayHandlerRegistry() {
        throw new UnsupportedOperationException();
    }
}
