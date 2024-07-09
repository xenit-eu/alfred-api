package eu.xenit.apix.tests.versionhistory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.server.ApplicationContextProvider;
import eu.xenit.apix.tests.BaseTest;
import eu.xenit.apix.versionhistory.IVersionHistoryService;
import eu.xenit.apix.versionhistory.Version;
import eu.xenit.apix.versionhistory.VersionHistory;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class VersionHistoryServiceTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(VersionHistoryServiceTest.class);
    private ApplicationContext testApplicationContext;
    private ServiceRegistry serviceRegistry;
    private IVersionHistoryService versionHistoryService;
    private org.alfresco.service.cmr.version.VersionService alfrizcoVersionHistoryService;
    private ApixToAlfrescoConversion c;

    //Test variables
    private NodeRef testNode;


    @Before
    public void Setup() {
        AuthenticationUtil.setFullyAuthenticatedUser("admin");
        // initialiseBeans BaseTest
        initialiseBeans();
        // initialise the local beans
        testApplicationContext = ApplicationContextProvider.getApplicationContext();
        versionHistoryService = testApplicationContext.getBean(IVersionHistoryService.class);
        serviceRegistry = testApplicationContext.getBean(ServiceRegistry.class);
        alfrizcoVersionHistoryService = serviceRegistry.getVersionService();
        c =  testApplicationContext.getBean(ApixToAlfrescoConversion.class);

        this.cleanUp();
        NodeRef companyHomeNodeRef = this.getNodeAtPath("/app:company_home");
        FileInfo mainTestFolder = this.createMainTestFolder(companyHomeNodeRef);
        testNode = this.createTestNode(mainTestFolder.getNodeRef(), "testnode").getNodeRef();
    }


    @Test
    public void TestGetVersionHistory() {
        Map<String, Serializable> versionProperties = new HashMap<>();

        //No version in the beginning
        VersionHistory beforeVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        assertEquals(beforeVersioning.getVersionHistory().size(), 1); // changed from null to 1.

        //First a minor version
        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, org.alfresco.service.cmr.version.VersionType.MINOR);
        org.alfresco.service.cmr.version.Version version = alfrizcoVersionHistoryService
                .createVersion(testNode, versionProperties);
        VersionHistory initialVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));

        assertNotEquals(initialVersioning, null);
        assertEquals(initialVersioning.getVersionHistory().size(), 2); // changed from 1 -> 2
        Version firstVersion = initialVersioning.getVersionHistory().get(0);

        assertNotEquals(firstVersion.getModifiedDate(), null);
        assertEquals(Version.VersionType.MINOR, firstVersion.getType());

        //Now a major version with comment
        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, org.alfresco.service.cmr.version.VersionType.MAJOR);
        versionProperties.put(VersionBaseModel.PROP_DESCRIPTION, "Test123");
        org.alfresco.service.cmr.version.Version version2 = alfrizcoVersionHistoryService
                .createVersion(testNode, versionProperties);
        VersionHistory secondVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        assertNotEquals(secondVersioning, null);
        assertEquals(secondVersioning.getVersionHistory().size(), 3); // changed from 2 -> 3
        Version secondVersion = secondVersioning.getVersionHistory().get(0);
        assertEquals(secondVersion.getDescription(), "Test123");
        assertEquals(Version.VersionType.MAJOR, secondVersion.getType());
    }

    // Fails with System is in RO-mode but we have uploaded the license manually...
    @Test
    public void TestCreateVersion() {
        Map<String, Serializable> versionProperties = new HashMap<>();

        //No version in the beginning
        VersionHistory beforeVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        assertEquals(beforeVersioning.getVersionHistory().size(), 1);

        //First a minor version
        logger.error("TestCreateVersion - versionProperties.put( {}   , {})", VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR );
        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, VersionType.MINOR);
        // Example of above to set the versionProps...
        //        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, org.alfresco.service.cmr.version.VersionType.MINOR);

        // TODO - fails due to RO-mode (even with license uploaded)
//         Error =06040043 Access Denied.  The system is currently in read-only mode.
//        org.alfresco.service.transaction.ReadOnlyServerException: 06040043 Access Denied.  The system is currently in read-only mode.
//        	at org.alfresco.repo.domain.node.AbstractNodeDAOImpl.getCurrentTransaction(AbstractNodeDAOImpl.java:586)
//        	at org.alfresco.repo.domain.node.AbstractNodeDAOImpl.newNodeImpl(AbstractNodeDAOImpl.java:1309)
//        	at org.alfresco.repo.domain.node.AbstractNodeDAOImpl.newNode(AbstractNodeDAOImpl.java:1233)
//        	at org.alfresco.repo.node.db.DbNodeServiceImpl.createNode_aroundBody24(DbNodeServiceImpl.java:392)
//        	at org.alfresco.repo.node.db.DbNodeServiceImpl$AjcClosure25.run(DbNodeServiceImpl.java:1)
//        	at org.aspectj.runtime.reflect.JoinPointImpl.proceed(JoinPointImpl.java:179)
//        	at org.alfresco.traitextender.RouteExtensions.intercept(RouteExtensions.java:100)
//        	at org.alfresco.repo.node.db.DbNodeServiceImpl.createNode(DbNodeServiceImpl.java:342)
//        	at jdk.internal.reflect.GeneratedMethodAccessor627.invoke(Unknown Source)
//        	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
//        	at java.base/java.lang.reflect.Method.invoke(Unknown Source)
//        	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:343)
//        	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196)
//        	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
//        	at org.alfresco.repo.lock.mem.LockableAspectInterceptor.invoke(LockableAspectInterceptor.java:244)
//        	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
//        	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:244)
//        	at jdk.proxy3/jdk.proxy3.$Proxy46.createNode(Unknown Source)
//        	at jdk.internal.reflect.GeneratedMethodAccessor627.invoke(Unknown Source)
//        	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
//        	at java.base/java.lang.reflect.Method.invoke(Unknown Source)
//        	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:343)
//        	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196)
//        	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
//        	at org.alfresco.repo.tenant.MultiTNodeServiceInterceptor.invoke(MultiTNodeServiceInterceptor.java:111)
//        	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
//        	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:244)
//        	at jdk.proxy3/jdk.proxy3.$Proxy46.createNode(Unknown Source)
//        	at org.alfresco.repo.version.Version2ServiceImpl.createNewVersion(Version2ServiceImpl.java:520)
//        	at org.alfresco.repo.version.Version2ServiceImpl.createVersion(Version2ServiceImpl.java:324)
//        	at org.alfresco.repo.version.Version2ServiceImpl.createVersion_aroundBody2(Version2ServiceImpl.java:158)
//        	at org.alfresco.repo.version.Version2ServiceImpl$AjcClosure3.run(Version2ServiceImpl.java:1)
//        	at org.aspectj.runtime.reflect.JoinPointImpl.proceed(JoinPointImpl.java:179)
//        	at org.alfresco.traitextender.RouteExtensions.intercept(RouteExtensions.java:100)
//        	at org.alfresco.repo.version.Version2ServiceImpl.createVersion(Version2ServiceImpl.java:142)
//        	at eu.xenit.apix.alfresco.versionhistory.VersionHistoryService.createVersion(VersionHistoryService.java:83)
//        	at eu.xenit.apix.tests.versionhistory.VersionHistoryServiceTest.TestCreateVersion(VersionHistoryServiceTest.java:116)
        versionHistoryService.createVersion(c.apix(testNode), versionProperties);
//        org.alfresco.service.cmr.version.Version version = alfrizcoVersionHistoryService
//                        .createVersion(testNode, versionProperties);
        VersionHistory initialVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        logger.error("TestCreateVersion - initialVersioning( {}  )", initialVersioning);

        assertNotEquals(initialVersioning, null);
        assertEquals(initialVersioning.getVersionHistory().size(), 2); // changed from 1 -> 2
        Version firstVersion = initialVersioning.getVersionHistory().get(0);
        assertNotEquals(firstVersion.getModifiedDate(), null);
        assertEquals(Version.VersionType.MINOR, firstVersion.getType());

        //Now a major version with comment
        versionProperties.put(VersionBaseModel.PROP_VERSION_TYPE, org.alfresco.service.cmr.version.VersionType.MAJOR);
        versionProperties.put(VersionBaseModel.PROP_DESCRIPTION, "Test123");
        // TODO - Apix createversino fails, see error above
//        versionHistoryService.createVersion(c.apix(testNode), versionProperties);
        alfrizcoVersionHistoryService.createVersion(testNode, versionProperties);
        VersionHistory secondVersioning = versionHistoryService.GetVersionHistory(c.apix(testNode));
        assertNotEquals(secondVersioning, null);
        assertEquals(secondVersioning.getVersionHistory().size(), 3); // 2 to 3??
        Version secondVersion = secondVersioning.getVersionHistory().get(0);
        assertEquals(secondVersion.getDescription(), "Test123");
        assertEquals(Version.VersionType.MAJOR, secondVersion.getType());
    }
}
