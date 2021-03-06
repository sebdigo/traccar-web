/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
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
package org.traccar.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.validator.MaxNumberValidator;
import com.sencha.gxt.widget.core.client.form.validator.MinNumberValidator;
import org.traccar.web.client.i18n.Messages;

public class TrackerServerLogViewDialog {
    private static TrackerServerLogViewDialogUiBinder uiBinder = GWT.create(TrackerServerLogViewDialogUiBinder.class);

    interface TrackerServerLogViewDialogUiBinder extends UiBinder<Widget, TrackerServerLogViewDialog> {
    }

    public interface LogHandler {
        void onLoad(short size, TextArea logArea);
    }

    final LogHandler logHandler;

    @UiField
    Window window;

    @UiField
    TextArea logArea;

    @UiField(provided = true)
    NumberPropertyEditor<Short> shortPropertyEditor = new NumberPropertyEditor.ShortPropertyEditor();

    @UiField
    NumberField<Short> logSize;

    @UiField(provided = true)
    Messages i18n = GWT.create(Messages.class);

    public TrackerServerLogViewDialog(String title, LogHandler logHandler) {
        this.logHandler = logHandler;

        uiBinder.createAndBindUi(this);

        window.setHeadingText(title);

        logSize.addValidator(new MinNumberValidator<>((short) 1));
        logSize.addValidator(new MaxNumberValidator<>((short) 16384));

        logSize.setValue((short) 100);

        refresh();
    }

    private void refresh() {
        logHandler.onLoad(logSize.getValue(), logArea);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    @UiHandler("refreshButton")
    public void onRefreshClicked(SelectEvent event) {
        refresh();
    }

    @UiHandler("closeButton")
    public void onCloseClicked(SelectEvent event) {
        hide();
    }
}
