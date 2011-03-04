package com.google.gwt.query.client.plugins.widgets;

import static com.google.gwt.query.client.GQuery.$;

import java.util.Date;

import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

/**
 * Factory used to create a {@link DateBox} widget. A {@link DateBox} is created
 * if the element is a <i>input</i> with type text, a <i>div</i> or a<i>span</i> element.
 * 
 * The content of the element has to be empty, a valid date or a valid date-format string.
 */
public class DateBoxWidgetFactory implements WidgetFactory<DateBox> {
  
  // DateBox needs to call the onAttach method
  public static class AttachableDateBox extends DateBox implements Attachable {
    public void attach(){
      onAttach();
      RootPanel.detachOnWindowClose(this);
    }
  }
  
  public DateBox create(Element e) {
    String v = null;
    if ($(e).filter("input[type='text']").size() == 1) {
      v = GQuery.$(e).val();
    } else if (WidgetsUtils.matchesTags(e, "div", "span")) {
      v = GQuery.$(e).text();
    }
    if (v!=null) {
      DateBox w = create(v);
      WidgetsUtils.replace(e, w);
      return w;
    }
    return null;
  }
  
  @SuppressWarnings("deprecation")
  private DateBox create(String v) {
    Date d = new Date();
    DateTimeFormat f = null;
    if (v != null) {
      try {
        d = new Date(v);
      } catch (Exception e) {
        try {
          f = DateTimeFormat.getFormat(v);
        } catch (Exception e2) {
        }
      }
    }
    DateBox b = new AttachableDateBox();
    b.setValue(d);
    if (f != null) {
      b.setFormat(new DefaultFormat(f));
    }
    return b;
  }
}