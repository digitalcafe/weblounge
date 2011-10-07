package ch.entwine.weblounge.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebloungeLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

  private static final String PATH_PARAMETER_NAME = "path";

  /**
   * {@inheritDoc}
   * 
   * @see org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler#onLogoutSuccess(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse,
   *      org.springframework.security.core.Authentication)
   */
  @Override
  public void onLogoutSuccess(HttpServletRequest request,
      HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    String targetUrl = DEFAULT_TARGET_PARAMETER;
    if (request.getParameter(PATH_PARAMETER_NAME) != null) {
      targetUrl = request.getParameter(PATH_PARAMETER_NAME);
    }
    setDefaultTargetUrl(addTimeStamp(targetUrl));
    super.onLogoutSuccess(request, response, authentication);
  }

  /**
   * Add a timestamp parameter to the url location
   * 
   * @param location
   *          the url
   * @return the page with a timestamp
   */
  private String addTimeStamp(String location) {
    long timeStamp = new Date().getTime();
    if (location.contains("?")) {
      return location.concat("&_=" + timeStamp);
    } else {
      return location.concat("?_=" + timeStamp);
    }
  }

}
