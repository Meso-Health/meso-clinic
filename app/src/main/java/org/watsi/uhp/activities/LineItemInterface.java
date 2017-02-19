package org.watsi.uhp.activities;

import org.watsi.uhp.models.LineItem;

import java.util.List;

public interface LineItemInterface {

    void setCurrentLineItems(List<LineItem> lineItems);

    List<LineItem> getCurrentLineItems();
}
