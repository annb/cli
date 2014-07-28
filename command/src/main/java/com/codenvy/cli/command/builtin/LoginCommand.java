/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.command.builtin;


import static org.fusesource.jansi.Ansi.Color.BLUE;
import static org.fusesource.jansi.Ansi.Color.CYAN;
import static org.fusesource.jansi.Ansi.Color.DEFAULT;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.fusesource.jansi.Ansi;

import com.codenvy.cli.security.FileBasedCredentialsProvider;
import com.codenvy.client.Codenvy;
import com.codenvy.client.CodenvyException;
import com.codenvy.client.auth.Credentials;

/**
 * Allows to be authenticated on Codenvy
 *
 * @author Florent Benoit
 * @author Stéphane Daviet
 */
@Command(scope = "codenvy", name = "login", description = "Login into Codenvy System")
public class LoginCommand extends AbsCommand {
    /**
     * Host on which to perform the authentication. (override host defined in the configuration file)
     */
    @Option(name = "-h", aliases = {"--host"}, description = "URL of codenvy server", required = false, multiValued = false)
    private String  host;

    /**
     * TODO: check is for ?
     */
    @Option(name = "-c", aliases = {"--check"}, description = "Check", required = false, multiValued = false)
    private boolean check;

    /**
     * Specify the username
     */
    @Argument(index = 0, name = "username", description = "Username", required = true, multiValued = false)
    private String  username;

    /**
     * Specify the password (override password defined in the configuration file)
     */
    @Argument(index = 1, name = "password", description = "Password", required = true, multiValued = false)
    private String  password;

    /**
     * Launch the authentication
     *
     * @return
     */
    @Override
    protected Object doExecute() {

        String url = host;
        if (url == null) {
            url = getCodenvyProperty("host");
        }

        // Manage credentials
        Credentials credentials = getCodenvyClient().newCredentialsBuilder()
                                                    .withUsername(username)
                                                    .withPassword(password)
                                                    .build();

        Codenvy codenvy = getCodenvyClient().newCodenvyBuilder(url, username)
                                            .withCredentials(credentials)
                                            .withCredentialsProvider(new FileBasedCredentialsProvider(username + '@' + host))
                                            .build();

        Ansi buffer = Ansi.ansi();

        buffer.a("Login ");

        // success or not ?
        try {
            // Login
            codenvy.user().current().execute();
            buffer.fg(GREEN);
            buffer.a("OK");
            buffer.reset();
            buffer.a(" : Welcome ");
            buffer.fg(CYAN);
            buffer.a(username);
            buffer.reset();
            // Keep the token object
            session.put(Codenvy.class.getName(), codenvy);
        } catch (CodenvyException e) {
            buffer.fg(RED);
            buffer.a("failed");
            buffer.reset();
            buffer.a(" : Unable to perform login : ");
            buffer.a(e.getMessage());
        }

        // print result
        System.out.println(buffer.toString());

        return null;
    }
}
