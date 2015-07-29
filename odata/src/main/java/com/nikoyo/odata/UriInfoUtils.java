package com.nikoyo.odata;


import com.nikoyo.odata.persistence.FilterVisitor;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.*;
import org.apache.olingo.server.core.uri.queryoption.SelectItemImpl;
import org.apache.olingo.server.core.uri.queryoption.SelectOptionImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class UriInfoUtils {


    public static EdmEntitySet getEdmEntitySet(UriInfoResource uriInfo) throws ODataApplicationException {

        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        if (!(resourcePaths.get(0) instanceof UriResourceEntitySet)) {
            throw new ODataApplicationException("Invalid resource type for first segment.", HttpStatusCode.NOT_IMPLEMENTED
                    .getStatusCode(), Locale.ENGLISH);
        }

        UriResourceEntitySet uriResource = (UriResourceEntitySet) resourcePaths.get(0);
        return uriResource.getEntitySet();
    }

    public static SelectOption getSelect(UriInfo uriInfo) {

        SelectOption selectOption = null;
        Collection<SystemQueryOption> systemQueryOptions = uriInfo.getSystemQueryOptions();
        for (SystemQueryOption systemQueryOption : systemQueryOptions) {
            if (systemQueryOption.getKind().equals(SystemQueryOptionKind.SELECT)) {
                selectOption = (SelectOption) systemQueryOption;
            }
        }

        if (selectOption == null) {
            selectOption = new SelectOptionImpl();
            ((SelectOptionImpl) selectOption).setSelectItems(new ArrayList<SelectItemImpl>() {{
                add(new SelectItemImpl().setStar(true));
            }});
        }

        return selectOption;
    }

    public static String getOrderBy(OrderByOption orderByOption) throws ODataApplicationException {
        String orderBy = StringUtils.EMPTY;
        if (orderByOption == null) return orderBy;
        FilterVisitor filterVisitor = new FilterVisitor();
        for (OrderByItem orderByItem : orderByOption.getOrders()) {
            orderBy += filterVisitor.visit(orderByItem.getExpression()).toString();
            if (orderByItem.isDescending()) orderBy += " desc,";
            else orderBy += " asc,";
        }
        if (StringUtils.isNotEmpty(orderBy))
            orderBy = "order by" + orderBy.substring(0, orderBy.length() - 1);

        return orderBy;
    }

}
