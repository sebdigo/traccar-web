/*
 * Copyright 2014 Mosco (info@mosco.in)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mosco.web.server.model;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import org.mosco.web.client.model.DataService;
import org.mosco.web.client.model.EventService;
import org.mosco.web.server.reports.ReportsModule;
import org.mosco.web.shared.model.ApplicationSettings;
import org.mosco.web.shared.model.Picture;
import org.mosco.web.shared.model.User;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class GuiceServletConfig extends GuiceServletContextListener {
    private static final String PERSISTENCE_DATASTORE = "java:/DefaultDS";
    private static final String PERSISTENCE_UNIT_DEBUG = "debug";
    private static final String PERSISTENCE_UNIT_RELEASE = "release";

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(getModule());
    }

    ServletModule getModule() {
        return new ServletModule() {
            @Override
            protected void configureServlets() {
                String persistenceUnit;
                boolean debug = false;
                try {
                    Context context = new InitialContext();
                    context.lookup(PERSISTENCE_DATASTORE);
                    persistenceUnit = PERSISTENCE_UNIT_RELEASE;
                } catch (NamingException e) {
                    persistenceUnit = PERSISTENCE_UNIT_DEBUG;
                    debug = true;
                }

                install(new JpaPersistModule(persistenceUnit));
                install(new ReportsModule());

                filter("/mosco/*").through(PersistFilter.class);
                filter("/", "/mosco.html", "/m/", "/m/index.html").through(LocaleFilter.class);

                serve("/mosco/dataService").with(DataServiceImpl.class);
                serve("/mosco/uiStateService").with(UIStateServiceImpl.class);
                serve("/mosco/eventService").with(EventServiceImpl.class);
                serve("/mosco/notificationService").with(NotificationServiceImpl.class);
                serve("/mosco/picturesService").with(PicturesServiceImpl.class);
                serve("/mosco/reportService").with(ReportServiceImpl.class);
                serve("/mosco/logService").with(LogServiceImpl.class);
                serve("/mosco/groupService").with(GroupServiceImpl.class);

                serve("/mosco/rest/*").with(RESTApiServlet.class);
                serve("/mosco/export/*").with(ExportServlet.class);
                serve("/mosco/import/*").with(ImportServlet.class);
                serve("/mosco/report*").with(ReportServlet.class);
                serve("/mosco/s/login").with(LoginServlet.class);
                serve("/" + Picture.URL_PREFIX + "*").with(PicturesServlet.class);

                if (debug) {
                    serve("/api*").with(BackendApiStubServlet.class);
                }

                UserCheck userCheck = new UserCheck();
                requestInjection(userCheck);

                bindInterceptor(Matchers.any(), Matchers.annotatedWith(RequireUser.class), userCheck);
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(ManagesDevices.class), userCheck);
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(RequireWrite.class), userCheck);

                MethodCallLogger methodCallLogger = new MethodCallLogger();
                requestInjection(methodCallLogger);
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(LogCall.class), methodCallLogger);

                BackendRefresher backendRefresher = new BackendRefresher();
                requestInjection(backendRefresher);
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(RefreshBackendPermissions.class), backendRefresher);

                bind(User.class).toProvider(CurrentUserProvider.class);
                bind(ApplicationSettings.class).toProvider(ApplicationSettingsProvider.class);
                bind(DataService.class).to(DataServiceImpl.class);
                bind(EventService.class).to(EventServiceImpl.class);

                bindInterceptor(Matchers.subclassesOf(RemoteServiceServlet.class),
                        Matchers.returns(Matchers.only(SerializationPolicy.class)), new FixSerializationPolicy());
            }
        };
    }
}
