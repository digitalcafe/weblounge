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

package ch.entwine.weblounge.security.sql.endpoint;

import static ch.entwine.weblounge.common.impl.util.doc.Status.badRequest;
import static ch.entwine.weblounge.common.impl.util.doc.Status.conflict;
import static ch.entwine.weblounge.common.impl.util.doc.Status.created;
import static ch.entwine.weblounge.common.impl.util.doc.Status.forbidden;
import static ch.entwine.weblounge.common.impl.util.doc.Status.notFound;
import static ch.entwine.weblounge.common.impl.util.doc.Status.notModified;
import static ch.entwine.weblounge.common.impl.util.doc.Status.ok;
import static ch.entwine.weblounge.common.impl.util.doc.Status.serviceUnavailable;

import ch.entwine.weblounge.common.impl.util.doc.Endpoint;
import ch.entwine.weblounge.common.impl.util.doc.Endpoint.Method;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.entwine.weblounge.common.impl.util.doc.Format;
import ch.entwine.weblounge.common.impl.util.doc.Parameter;
import ch.entwine.weblounge.common.impl.util.doc.TestForm;

/**
 * SQL directory provider endpoint documentation generator.
 */
public final class SQLDirectoryProviderEndpointDocs {

  /**
   * No need to instantiate this utility class.
   */
  private SQLDirectoryProviderEndpointDocs() {
  }

  /**
   * Creates the documentation.
   * 
   * @param endpointUrl
   *          the endpoint address
   * @return the endpoint documentation
   */
  public static String createDocumentation(String endpointUrl) {
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "sqldirectoryprovider");
    docs.setTitle("Weblounge SQL Directory Provider");

    // GET /
    Endpoint getStatus = new Endpoint("/", Method.GET, "status");
    getStatus.setDescription("Returns the directory provider status");
    getStatus.addFormat(Format.xml());
    getStatus.addStatus(ok("status has been sent back to the client"));
    getStatus.addStatus(notFound("the site does not exist"));
    getStatus.addStatus(serviceUnavailable("the site is temporarily offline"));
    getStatus.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getStatus);

    // PUT /status
    Endpoint enableSiteLogins = new Endpoint("/status", Method.PUT, "enable");
    enableSiteLogins.setDescription("Enables login to the site");
    enableSiteLogins.addFormat(Format.xml());
    enableSiteLogins.addStatus(ok("site logins have been enabled"));
    enableSiteLogins.addStatus(notModified("site logins were already enabled"));
    enableSiteLogins.addStatus(notFound("the site does not exist"));
    enableSiteLogins.addStatus(serviceUnavailable("the site is temporarily offline"));
    enableSiteLogins.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, enableSiteLogins);

    // DELETE /status
    Endpoint disableSiteLogins = new Endpoint("/status", Method.DELETE, "disable");
    disableSiteLogins.setDescription("Disables login to the site");
    disableSiteLogins.addFormat(Format.xml());
    disableSiteLogins.addStatus(ok("site logins have been disabled"));
    disableSiteLogins.addStatus(notModified("site logins were already disabled"));
    disableSiteLogins.addStatus(notFound("the site does not exist"));
    disableSiteLogins.addStatus(serviceUnavailable("the site is temporarily offline"));
    disableSiteLogins.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, disableSiteLogins);

    // POST /account
    Endpoint createAccount = new Endpoint("/account", Method.POST, "createaccount");
    createAccount.setDescription("Creates a new account in this site");
    createAccount.addFormat(Format.xml());
    createAccount.addStatus(created("the account has been created and the account's location is part of the response"));
    createAccount.addStatus(conflict("an account with that login already exists"));
    createAccount.addStatus(badRequest("the login is malformed or empty"));
    createAccount.addStatus(notFound("the site does not exist"));
    createAccount.addStatus(serviceUnavailable("the site is temporarily offline"));
    createAccount.setTestForm(new TestForm());
    createAccount.addRequiredParameter(new Parameter("login", Parameter.Type.String, "Unique login name"));
    createAccount.addRequiredParameter(new Parameter("password", Parameter.Type.Password, "Password"));
    createAccount.addOptionalParameter(new Parameter("email", Parameter.Type.String, "E-mail address"));
    docs.addEndpoint(Endpoint.Type.WRITE, createAccount);

    // GET /account/{login}
    Endpoint getAccount = new Endpoint("/account/{login}", Method.GET, "getaccount");
    getAccount.setDescription("Returns the account");
    getAccount.addFormat(Format.xml());
    getAccount.addStatus(ok("the account information has been sent back to the client"));
    getAccount.addStatus(forbidden("insufficient rights to access the account"));
    getAccount.addStatus(notFound("the account does not exist"));
    getAccount.addStatus(notFound("the site does not exist"));
    getAccount.addStatus(serviceUnavailable("the site is temporarily offline"));
    getAccount.setTestForm(new TestForm());
    getAccount.addPathParameter(new Parameter("login", Parameter.Type.String, "Login name"));
    docs.addEndpoint(Endpoint.Type.READ, getAccount);

    // PUT /account/{login}
    Endpoint updateAccount = new Endpoint("/account/{login}", Method.PUT, "updateaccount");
    updateAccount.setDescription("Updates the account");
    updateAccount.addFormat(Format.xml());
    updateAccount.addStatus(ok("the account has been updated"));
    updateAccount.addStatus(forbidden("insufficient rights to update the account"));
    updateAccount.addStatus(badRequest("if a non-existing language identifier is provided"));
    updateAccount.addStatus(notFound("the account does not exist"));
    updateAccount.addStatus(notFound("the site does not exist"));
    updateAccount.addStatus(serviceUnavailable("the site is temporarily offline"));
    updateAccount.setTestForm(new TestForm());
    updateAccount.addPathParameter(new Parameter("login", Parameter.Type.String, "Unique login name"));
    updateAccount.addOptionalParameter(new Parameter("email", Parameter.Type.String, "E-mail address"));
    updateAccount.addOptionalParameter(new Parameter("firstname", Parameter.Type.String, "First name"));
    updateAccount.addOptionalParameter(new Parameter("lastname", Parameter.Type.String, "Last name"));
    updateAccount.addOptionalParameter(new Parameter("initials", Parameter.Type.String, "Initials"));
    updateAccount.addOptionalParameter(new Parameter("language", Parameter.Type.String, "Two letter ISO code for the preferred language"));
    docs.addEndpoint(Endpoint.Type.WRITE, updateAccount);

    // PUT /account/{login}/password
    Endpoint updateAccountPassword = new Endpoint("/account/{login}/password", Method.PUT, "updateaccountpassword");
    updateAccountPassword.setDescription("Updates the account password");
    updateAccountPassword.addFormat(Format.xml());
    updateAccountPassword.addStatus(ok("the password has been updated"));
    updateAccountPassword.addStatus(forbidden("insufficient rights to update the account"));
    updateAccountPassword.addStatus(notFound("the account does not exist"));
    updateAccountPassword.addStatus(notFound("the site does not exist"));
    updateAccountPassword.addStatus(serviceUnavailable("the site is temporarily offline"));
    updateAccountPassword.setTestForm(new TestForm());
    updateAccountPassword.addPathParameter(new Parameter("login", Parameter.Type.String, "Unique login name"));
    updateAccountPassword.addOptionalParameter(new Parameter("password", Parameter.Type.Password, "Password"));
    docs.addEndpoint(Endpoint.Type.WRITE, updateAccountPassword);

    // PUT /account/{login}/challenge
    Endpoint updateAccountChallenge = new Endpoint("/account/{login}/challenge", Method.PUT, "updateaccountchallenge");
    updateAccountChallenge.setDescription("Updates the account challenge");
    updateAccountChallenge.addFormat(Format.xml());
    updateAccountChallenge.addStatus(ok("the challenge has been updated"));
    updateAccountChallenge.addStatus(forbidden("insufficient rights to update the account"));
    updateAccountChallenge.addStatus(notFound("the account does not exist"));
    updateAccountChallenge.addStatus(notFound("the site does not exist"));
    updateAccountChallenge.addStatus(serviceUnavailable("the site is temporarily offline"));
    updateAccountChallenge.setTestForm(new TestForm());
    updateAccountChallenge.addPathParameter(new Parameter("login", Parameter.Type.String, "Unique login name"));
    updateAccountChallenge.addOptionalParameter(new Parameter("challenge", Parameter.Type.String, "Password"));
    updateAccountChallenge.addOptionalParameter(new Parameter("response", Parameter.Type.Password, "Password"));
    docs.addEndpoint(Endpoint.Type.WRITE, updateAccountChallenge);

    // DELETE /account/{login}
    Endpoint deleteAccount = new Endpoint("/account/{login}", Method.DELETE, "removeaccount");
    deleteAccount.setDescription("Removes the account from the site");
    deleteAccount.addFormat(Format.xml());
    deleteAccount.addStatus(ok("the account has been removed"));
    deleteAccount.addStatus(notFound("the account does not exist"));
    deleteAccount.addStatus(notFound("the site does not exist"));
    deleteAccount.addStatus(serviceUnavailable("the site is temporarily offline"));
    deleteAccount.setTestForm(new TestForm());
    deleteAccount.addPathParameter(new Parameter("login", Parameter.Type.String, "Login name"));
    docs.addEndpoint(Endpoint.Type.WRITE, deleteAccount);

    // PUT /account/{login}/status
    Endpoint enableAccount = new Endpoint("/account/{login}/status", Method.PUT, "enableaccount");
    enableAccount.setDescription("Enables the account");
    enableAccount.addFormat(Format.xml());
    enableAccount.addStatus(ok("the account has been enabled"));
    enableAccount.addStatus(notModified("the account was already enabled"));
    enableAccount.addStatus(forbidden("insufficient rights to enable the account"));
    enableAccount.addStatus(notFound("the account does not exist"));
    enableAccount.addStatus(notFound("the site does not exist"));
    enableAccount.addStatus(serviceUnavailable("the site is temporarily offline"));
    enableAccount.setTestForm(new TestForm());
    enableAccount.addPathParameter(new Parameter("login", Parameter.Type.String, "Unique login name"));
    docs.addEndpoint(Endpoint.Type.WRITE, enableAccount);

    // DELETE /account/{login}/status
    Endpoint disableAccount = new Endpoint("/account/{login}/status", Method.DELETE, "disableaccount");
    disableAccount.setDescription("Disables the account");
    disableAccount.addFormat(Format.xml());
    disableAccount.addStatus(ok("the account has been disabled"));
    disableAccount.addStatus(notModified("the account is already disabled"));
    disableAccount.addStatus(forbidden("insufficient rights to disable the account"));
    disableAccount.addStatus(notFound("the account does not exist"));
    disableAccount.addStatus(notFound("the site does not exist"));
    disableAccount.addStatus(serviceUnavailable("the site is temporarily offline"));
    disableAccount.setTestForm(new TestForm());
    disableAccount.addPathParameter(new Parameter("login", Parameter.Type.String, "Login name"));
    docs.addEndpoint(Endpoint.Type.WRITE, disableAccount);

    // POST /account/{login}/roles/{context}
    Endpoint addRole = new Endpoint("/account/{login}/roles/{context}", Method.POST, "addrole");
    addRole.setDescription("Adds a role to the account");
    addRole.addFormat(Format.xml());
    addRole.addStatus(ok("the role has been added"));
    addRole.addStatus(notModified("the role is already owned by the account"));
    addRole.addStatus(badRequest("the role parameter is blank"));
    addRole.addStatus(forbidden("insufficient rights to add the role"));
    addRole.addStatus(notFound("the account does not exist"));
    addRole.addStatus(notFound("the site does not exist"));
    addRole.addStatus(serviceUnavailable("the site is temporarily offline"));
    addRole.setTestForm(new TestForm());
    addRole.addPathParameter(new Parameter("login", Parameter.Type.String, "Unique login name"));
    addRole.addPathParameter(new Parameter("context", Parameter.Type.String, "Role context"));
    addRole.addRequiredParameter(new Parameter("role", Parameter.Type.String, "Role name"));
    docs.addEndpoint(Endpoint.Type.WRITE, addRole);

    // DELETE /account/{login}/roles/{context}
    Endpoint removeRole = new Endpoint("/account/{login}/roles/{context}", Method.DELETE, "removerole");
    removeRole.setDescription("Removes a role from the account");
    removeRole.addFormat(Format.xml());
    removeRole.addStatus(ok("the role has been removed"));
    removeRole.addStatus(notModified("the role had not been owned by the account"));
    removeRole.addStatus(badRequest("the role parameter is blank"));
    removeRole.addStatus(forbidden("insufficient rights to remove the role"));
    removeRole.addStatus(notFound("the account does not exist"));
    removeRole.addStatus(notFound("the site does not exist"));
    removeRole.addStatus(serviceUnavailable("the site is temporarily offline"));
    removeRole.setTestForm(new TestForm());
    removeRole.addPathParameter(new Parameter("login", Parameter.Type.String, "Unique login name"));
    removeRole.addPathParameter(new Parameter("context", Parameter.Type.String, "Role context"));
    removeRole.addRequiredParameter(new Parameter("role", Parameter.Type.String, "Role name"));
    docs.addEndpoint(Endpoint.Type.WRITE, removeRole);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
