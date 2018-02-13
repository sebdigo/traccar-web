package org.mosco.web.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import org.mosco.web.shared.model.NotificationSettings;
import org.mosco.web.shared.model.NotificationTemplate;

public interface NotificationServiceAsync {
    void checkEmailSettings(NotificationSettings settings, AsyncCallback<Void> async);

    void getSettings(AsyncCallback<NotificationSettings> async);

    void saveSettings(NotificationSettings settings, AsyncCallback<Void> async);

    void checkPushbulletSettings(NotificationSettings settings, AsyncCallback<Void> async);

    void checkTemplate(NotificationTemplate template, AsyncCallback<String> async);
}
