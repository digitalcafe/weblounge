/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.entwine.weblounge.dispatcher.impl.handler;

import static ch.entwine.weblounge.common.Times.MS_PER_DAY;
import static ch.entwine.weblounge.common.impl.security.WebloungePermissionUtils.checkResourceReadPermission;
import static java.lang.String.format;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.image.ImageContent;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.impl.content.image.ImageResourceURIImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.security.PermissionException;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.dispatcher.RequestHandler;
import ch.entwine.weblounge.dispatcher.impl.DispatchUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * This request handler is used to handle requests to scaled images in the
 * repository.
 */
public final class ImageRequestHandlerImpl implements RequestHandler {

  /** Alternate uri prefix */
  protected static final String URI_PREFIX = "/weblounge-images/";

  /** Name of the image style parameter */
  protected static final String OPT_IMAGE_STYLE = "style";

  /** Length of a UUID */
  protected static final int UUID_LENGTH = 36;

  /** The server environment */
  protected Environment environment = Environment.Production;

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(ImageRequestHandlerImpl.class);

  /**
   * Handles the request for an image resource that is believed to be in the
   * content repository. The handler scales the image as requested, sets the
   * response headers and the writes the image contents to the response.
   * <p>
   * This method returns <code>true</code> if the handler is decided to handle
   * the request, <code>false</code> otherwise.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   */
  public boolean service(WebloungeRequest request, WebloungeResponse response) {

    WebUrl url = request.getUrl();
    Site site = request.getSite();
    String path = url.getPath();
    String fileName = null;

    // Get hold of the content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      return false;
    } else if (contentRepository.isIndexing()) {
      logger.debug("Content repository of site '{}' is currently being indexed", site);
      DispatchUtils.sendServiceUnavailable(request, response);
      return true;
    }

    // Check if the request uri matches the special uri for images. If so, try
    // to extract the id from the last part of the path. If not, check if there
    // is an image with the current path.
    ResourceURI imageURI = null;
    ImageResource imageResource = null;
    try {
      String id = null;
      String imagePath = null;

      if (path.startsWith(URI_PREFIX)) {
        String uriSuffix = StringUtils.chomp(path.substring(URI_PREFIX.length()), "/");
        uriSuffix = URLDecoder.decode(uriSuffix, "utf-8");

        // Check whether we are looking at a uuid or a url path
        if (uriSuffix.length() == UUID_LENGTH) {
          id = uriSuffix;
        } else if (uriSuffix.length() >= UUID_LENGTH) {
          int lastSeparator = uriSuffix.indexOf('/');
          if (lastSeparator == UUID_LENGTH && uriSuffix.indexOf('/', lastSeparator + 1) < 0) {
            id = uriSuffix.substring(0, lastSeparator);
            fileName = uriSuffix.substring(lastSeparator + 1);
          } else {
            imagePath = uriSuffix;
            fileName = FilenameUtils.getName(imagePath);
          }
        } else {
          imagePath = "/" + uriSuffix;
          fileName = FilenameUtils.getName(imagePath);
        }
      } else {
        imagePath = path;
        fileName = FilenameUtils.getName(imagePath);
      }

      // Try to load the resource
      imageURI = new ImageResourceURIImpl(site, imagePath, id);
      imageResource = contentRepository.get(imageURI);
      if (imageResource == null) {
        logger.debug("No image found at {}", imageURI);
        return false;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error loading image from {}: {}", contentRepository, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    } catch (UnsupportedEncodingException e) {
      logger.error("Error decoding image url {} using utf-8: {}", path, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    }

    // Agree to serve the image
    logger.debug("Image handler agrees to handle {}", path);

    // Check the request method. Only GET is supported right now.
    String requestMethod = request.getMethod().toUpperCase();
    if ("OPTIONS".equals(requestMethod)) {
      String verbs = "OPTIONS,GET";
      logger.trace("Answering options request to {} with {}", url, verbs);
      response.setHeader("Allow", verbs);
      response.setContentLength(0);
      return true;
    } else if (!"GET".equals(requestMethod)) {
      logger.debug("Image request handler does not support {} requests", requestMethod);
      DispatchUtils.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, request, response);
      return true;
    }

    // Is it published?
    // TODO: Fix this. imageResource.isPublished() currently returns false,
    // as both from and to dates are null (see PublishingCtx)
    // if (!imageResource.isPublished()) {
    // logger.debug("Access to unpublished image {}", imageURI);
    // DispatchUtils.sendNotFound(request, response);
    // return true;
    // }

    // Can the image be accessed by the current user?
    User user = request.getUser();
    try {
      checkResourceReadPermission(user, imageResource);
    } catch (PermissionException e) {
      logger.warn("Access to image {} denied for user {}", imageURI, user);
      DispatchUtils.sendAccessDenied(request, response);
      return true;
    }

    // Determine the response language by filename
    Language language = null;
    if (StringUtils.isNotBlank(fileName)) {
      for (ImageContent c : imageResource.contents()) {
        if (c.getFilename().equalsIgnoreCase(fileName)) {
          if (language != null) {
            logger.debug("Unable to determine language from ambiguous filename");
            language = LanguageUtils.getPreferredContentLanguage(imageResource, request, site);
            break;
          }
          language = c.getLanguage();
        }
      }
      if (language == null)
        language = LanguageUtils.getPreferredContentLanguage(imageResource, request, site);
    } else {
      language = LanguageUtils.getPreferredContentLanguage(imageResource, request, site);
    }

    // If the filename did not lead to a language, apply language resolution
    if (language == null) {
      logger.warn("Image {} does not exist in any supported language", imageURI);
      DispatchUtils.sendNotFound(request, response);
      return true;
    }

    // Extract the image style and scale the image
    String styleId = StringUtils.trimToNull(request.getParameter(OPT_IMAGE_STYLE));
    if (styleId != null) {
      try {
        StringBuffer redirect = new StringBuffer(PathUtils.concat(PreviewRequestHandlerImpl.URI_PREFIX, imageResource.getURI().getIdentifier()));
        redirect.append("?style=").append(styleId);
        response.sendRedirect(redirect.toString());
      } catch (Throwable t) {
        logger.debug("Error sending redirect to the client: {}", t.getMessage());
      }
      return true;
    }

    // Check the modified headers
    long revalidationTime = MS_PER_DAY;
    long expirationDate = System.currentTimeMillis() + revalidationTime;
    if (!ResourceUtils.hasChanged(request, imageResource, language)) {
      logger.debug("Image {} was not modified", imageURI);
      response.setDateHeader("Expires", expirationDate);
      DispatchUtils.sendNotModified(request, response);
      return true;
    }

    // Load the image contents from the repository
    ImageContent imageContents = imageResource.getContent(language);

    // Add mime type header
    String contentType = imageContents.getMimetype();
    if (contentType == null)
      contentType = MediaType.APPLICATION_OCTET_STREAM;

    // Set the content type
    String characterEncoding = response.getCharacterEncoding();
    if (StringUtils.isNotBlank(characterEncoding))
      response.setContentType(contentType + "; charset=" + characterEncoding.toLowerCase());
    else
      response.setContentType(contentType);

    // Browser caches and proxies are allowed to keep a copy
    response.setHeader("Cache-Control", "public, max-age=" + revalidationTime);

    // Set Expires header
    response.setDateHeader("Expires", expirationDate);

    // Determine the resource's modification date
    long resourceLastModified = ResourceUtils.getModificationDate(imageResource, language).getTime();

    // Add last modified header
    response.setDateHeader("Last-Modified", resourceLastModified);

    // Add ETag header
    response.setHeader("ETag", ResourceUtils.getETagValue(imageResource));

    // Load the input stream from the repository
    InputStream imageInputStream = null;
    try {
      imageInputStream = contentRepository.getContent(imageURI, language);
    } catch (Throwable t) {
      logger.error("Error loading {} image '{}' from {}: {}", new Object[] {
          language,
          imageResource,
          contentRepository,
          t.getMessage() });
      logger.error(t.getMessage(), t);
      IOUtils.closeQuietly(imageInputStream);
      return false;
    }
    if (imageInputStream == null) {
      DispatchUtils.sendNotFound(format("Image %s not found", imageURI.getIdentifier()), request, response);
      return false;
    }

    // Write the image back to the client
    try {
      response.setHeader("Content-Length", Long.toString(imageContents.getSize()));
      response.setHeader("Content-Disposition", "inline; filename=" + imageContents.getFilename());
      IOUtils.copy(imageInputStream, response.getOutputStream());
      response.getOutputStream().flush();
    } catch (EOFException e) {
      logger.debug("Error writing image '{}' back to client: connection closed by client", imageResource);
      return true;
    } catch (IOException e) {
      if (RequestUtils.isCausedByClient(e))
        return true;
      logger.error("Error writing {} image '{}' back to client: {}", new Object[] {
          language,
          imageResource,
          e.getMessage() });
    } finally {
      IOUtils.closeQuietly(imageInputStream);
    }
    return true;

  }

  /**
   * Sets the server environment.
   * 
   * @param environment
   *          the server environment
   */
  void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * @see ch.entwine.weblounge.dispatcher.api.request.RequestHandler#getName()
   */
  public String getName() {
    return "image request handler";
  }

  /**
   * Returns a string representation of this request handler.
   * 
   * @return the handler name
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#getPriority()
   */
  public int getPriority() {
    return 0;
  }

}
