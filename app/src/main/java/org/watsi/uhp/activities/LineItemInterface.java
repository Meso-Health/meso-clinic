package org.watsi.uhp.activities;

import org.watsi.uhp.models.LineItem;

import java.util.List;

public interface LineItemInterface {

    public void setCurrentLineItems();

    public List<LineItem> getCurrentLineItems();
}
