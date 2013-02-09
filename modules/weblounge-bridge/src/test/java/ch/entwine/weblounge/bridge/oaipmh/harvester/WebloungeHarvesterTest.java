package ch.entwine.weblounge.bridge.oaipmh.harvester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import ch.entwine.weblounge.bridge.oaipmh.MatterhornRecordHandler;
import ch.entwine.weblounge.bridge.oaipmh.WebloungeHarvester;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.security.SiteAdminImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.impl.site.SiteURLImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.contentrepository.impl.FileResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.ImageResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.MovieResourceSerializer;
import ch.entwine.weblounge.contentrepository.impl.PageSerializer;
import ch.entwine.weblounge.contentrepository.impl.ResourceSerializerServiceImpl;
import ch.entwine.weblounge.contentrepository.impl.fs.FileSystemContentRepository;
import ch.entwine.weblounge.contentrepository.impl.index.elasticsearch.ElasticSearchUtils;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Test case for {@link WebloungeHarvester}
 */
public class WebloungeHarvesterTest {

  /** sample metadata prefix */
  private static final String METADATA_PREFIX = "matterhorn";

  /** The content repository */
  private FileSystemContentRepository contentRepository;

  /** The repository root directory */
  private File repositoryRoot;

  /** The mock site */
  private static Site site;

  /** The resource serializer */
  private static ResourceSerializerServiceImpl serializer = null;

  /**
   * Sets up static test data.
   * 
   * @throws MalformedURLException
   */
  @BeforeClass
  public static void setUpClass() throws MalformedURLException {
    // Resource serializer
    serializer = new ResourceSerializerServiceImpl();
    serializer.addSerializer(new PageSerializer());
    serializer.addSerializer(new FileResourceSerializer());
    serializer.addSerializer(new ImageResourceSerializer());
    serializer.addSerializer(new MovieResourceSerializer());

    // Template
    PageTemplate template = EasyMock.createNiceMock(PageTemplate.class);
    EasyMock.expect(template.getIdentifier()).andReturn("templateid").anyTimes();
    EasyMock.expect(template.getStage()).andReturn("non-existing").anyTimes();
    EasyMock.replay(template);

    Set<Language> languages = new HashSet<Language>();
    languages.add(LanguageUtils.getLanguage("en"));
    languages.add(LanguageUtils.getLanguage("de"));

    site = EasyMock.createNiceMock(Site.class);
    EasyMock.expect(site.getIdentifier()).andReturn("test").anyTimes();
    EasyMock.expect(site.getTemplate((String) EasyMock.anyObject())).andReturn(template).anyTimes();
    EasyMock.expect(site.getDefaultTemplate()).andReturn(template).anyTimes();
    EasyMock.expect(site.getHostname((Environment) EasyMock.anyObject())).andReturn(new SiteURLImpl(new URL("http://localhost/")));
    EasyMock.expect(site.getLanguages()).andReturn(languages.toArray(new Language[languages.size()])).anyTimes();
    EasyMock.expect(site.getModules()).andReturn(new Module[] {}).anyTimes();
    EasyMock.expect(site.getDefaultLanguage()).andReturn(LanguageUtils.getLanguage("de")).anyTimes();
    EasyMock.expect(site.getAdministrator()).andReturn(new SiteAdminImpl("admin")).anyTimes();
    EasyMock.replay(site);
  }

  /**
   * Set up Mock site and content repository
   * 
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    String rootPath = PathUtils.concat(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
    repositoryRoot = new File(rootPath);
    FileUtils.deleteQuietly(repositoryRoot);

    // Create the index configuration
    System.setProperty("weblounge.home", rootPath);
    ElasticSearchUtils.createIndexConfigurationAt(repositoryRoot);

    // Connect to the repository
    contentRepository = new FileSystemContentRepository();
    contentRepository.setSerializer(serializer);
    contentRepository.setEnvironment(Environment.Production);
    Dictionary<String, Object> repositoryProperties = new Hashtable<String, Object>();
    repositoryProperties.put(FileSystemContentRepository.OPT_ROOT_DIR, repositoryRoot.getAbsolutePath());
    contentRepository.updated(repositoryProperties);
    contentRepository.connect(site);
  }

  /**
   * Does the cleanup after each test.
   */
  @After
  public void tearDown() {
    try {
      contentRepository.disconnect();
      FileUtils.deleteQuietly(repositoryRoot);
    } catch (ContentRepositoryException e) {
      fail("Error disconnecting content repository: " + e.getMessage());
    }
  }

  /**
   * Test adding and deleting with the Matterhorn record handler
   * 
   * @throws Exception
   */
  @Test
  public void testMatterhornRecordHandler() throws Exception {
    UserImpl harvesterUser = new UserImpl("testlogin", site.getIdentifier(), "Harvester");
    RecordHandler recordHandler = new MatterhornRecordHandler(site, contentRepository, harvesterUser, "presentation/delivery", "presenter/delivery", "dublincore/episode", "dublincore/series");

    assertEquals(METADATA_PREFIX, recordHandler.getMetadataPrefix());
    assertEquals(1, contentRepository.getResourceCount());

    URL resource = this.getClass().getResource("test.webm");

    // Adds new records to content repository
    ListRecordsResponse response = new ListRecordsResponse(loadDoc("matterhorn-list-records-response.xml"));
    if (!response.isError()) {
      NodeList records = response.getRecords();
      for (int i = 0; i < records.getLength(); i++) {
        recordHandler.handle(replaceSource(records.item(i), resource));
      }
    }

    assertEquals(3, contentRepository.getResourceCount());

    // Update records
    response = new ListRecordsResponse(loadDoc("matterhorn-modified-list-records-response.xml"));
    if (!response.isError()) {
      NodeList records = response.getRecords();
      for (int i = 0; i < records.getLength(); i++) {
        recordHandler.handle(replaceSource(records.item(i), resource));
      }
    }

    assertEquals(3, contentRepository.getResourceCount());

    // Remove deleted records to content repository
    response = new ListRecordsResponse(loadDoc("matterhorn-deleted-list-records-response.xml"));
    if (!response.isError()) {
      NodeList records = response.getRecords();
      for (int i = 0; i < records.getLength(); i++) {
        recordHandler.handle(records.item(i));
      }
    }

    assertEquals(2, contentRepository.getResourceCount());
  }

  /**
   * Replace the source url.
   * 
   * @param item
   *          the node item
   * @param url
   *          the url to replace
   * @return the replaced node item
   */
  private Node replaceSource(Node item, URL resource) {
    Element elem = (Element) item;
    NodeList urls = elem.getElementsByTagName("url");
    for (int i = 0; i < urls.getLength(); i++) {
      urls.item(i).getFirstChild().setNodeValue(resource.toString());
    }
    return item;
  }

  /**
   * Load a xml file to a document
   * 
   * @param name
   *          the xml filename
   * @return the xml document
   * @throws Exception
   *           if xml document could not be created
   */
  public Document loadDoc(String name) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    return factory.newDocumentBuilder().parse(this.getClass().getResourceAsStream(name));
  }

}
