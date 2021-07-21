/*
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.entando.entando.plugins.jpsolr.aps.system.content.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agiletec.aps.system.RequestContext;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.common.tree.ITreeNodeManager;
import com.agiletec.aps.system.services.authorization.IAuthorizationManager;
import com.agiletec.aps.system.services.group.Group;
import com.agiletec.aps.system.services.page.Widget;
import com.agiletec.aps.system.services.user.UserDetails;
import com.agiletec.aps.util.ApsWebApplicationUtils;
import com.agiletec.plugins.jacms.aps.system.JacmsSystemConstants;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.SmallContentType;
import org.entando.entando.aps.system.services.searchengine.FacetedContentsResult;
import org.entando.entando.aps.system.services.searchengine.SearchEngineFilter;
import org.entando.entando.aps.system.services.searchengine.SearchEngineFilter.TextSearchOption;
import org.entando.entando.ent.exception.EntException;
import org.entando.entando.plugins.jpsolr.aps.system.JpSolrSystemConstants;
import org.entando.entando.plugins.jpsolr.aps.system.content.IAdvContentFacetManager;
import org.entando.entando.plugins.jpsolr.aps.system.solr.model.SolrFields;

/**
 * @author E.Santoboni
 */
public class FacetNavHelper implements IFacetNavHelper {
    
	private ITreeNodeManager treeNodeManager;
    
    private IAdvContentFacetManager advContentFacetManager;

    @Override
    public FacetedContentsResult getResult(List<String> selectedFacetNodes, RequestContext reqCtx) throws EntException {
        List<String> contentTypesFilter = this.getContentTypesFilter(reqCtx);
		List<String> userGroupCodes = new ArrayList<>(this.getAllowedGroups(reqCtx));
        SearchEngineFilter typeFilter = SearchEngineFilter.createAllowedValuesFilter(SolrFields.SOLR_CONTENT_TYPE_CODE_FIELD_NAME, false, contentTypesFilter, TextSearchOption.EXACT);
        SearchEngineFilter[] filters = new SearchEngineFilter[]{typeFilter};
        return this.getAdvContentFacetManager().getFacetResult(filters, selectedFacetNodes, null, userGroupCodes);
    }
	
    @Deprecated
	@Override
	public List<String> getSearchResult(List<String> selectedFacetNodes, RequestContext reqCtx) throws EntException {
        FacetedContentsResult result = this.getResult(selectedFacetNodes, reqCtx);
        return result.getContentsId();
	}
	
	/**
	 * Returns Content types filter
	 * @param reqCtx
	 * @return content types filter
	 * @throws EntException
	 */
	private List<String> getContentTypesFilter(RequestContext reqCtx) throws EntException {
		List<String> contentTypes = new ArrayList<>();
		Widget currentWidget = (Widget) reqCtx.getExtraParam(SystemConstants.EXTRAPAR_CURRENT_WIDGET);
		if (null == currentWidget.getConfig()) {
            return contentTypes;
        }
		String paramName = JpSolrSystemConstants.CONTENT_TYPES_FILTER_WIDGET_PARAM_NAME;
		String contentTypesParamValue = currentWidget.getConfig().getProperty(paramName);
		if (null != contentTypesParamValue) {
			IContentManager contentManager = (IContentManager) ApsWebApplicationUtils.getBean(JacmsSystemConstants.CONTENT_MANAGER, reqCtx.getRequest());
            Map<String, SmallContentType> types = contentManager.getSmallContentTypesMap();
			String[] contentTypesArray = contentTypesParamValue.split(",");
			for (int i=0; i<contentTypesArray.length; i++) {
				String contentTypeCode = contentTypesArray[i].trim();
				if (null != types.get(contentTypeCode)) {
					contentTypes.add(contentTypeCode);
				}
			}
		}
		return contentTypes;
	}

    @Deprecated
	@Override
	public Map<String, Integer> getOccurences(List<String> selectedFacetNodes, RequestContext reqCtx) throws EntException {
		FacetedContentsResult result = this.getResult(selectedFacetNodes, reqCtx);
        return result.getOccurrences();
	}

	/**
	 * Returns allowed groups
	 * @param reqCtx The request context
	 * @return allowed groups
	 */
	private Collection<String> getAllowedGroups(RequestContext reqCtx) {
		IAuthorizationManager authManager = (IAuthorizationManager) ApsWebApplicationUtils.getBean(SystemConstants.AUTHORIZATION_SERVICE, reqCtx.getRequest());
		UserDetails currentUser = (UserDetails) reqCtx.getRequest().getSession().getAttribute(SystemConstants.SESSIONPARAM_CURRENT_USER);
		List<Group> groups = authManager.getUserGroups(currentUser);
		Set<String> allowedGroup = new HashSet<>();
		Iterator<Group> iter = groups.iterator();
		while (iter.hasNext()) {
			Group group = iter.next();
			allowedGroup.add(group.getName());
		}
		allowedGroup.add(Group.FREE_GROUP_NAME);
		return allowedGroup;
	}

	@Override
	public ITreeNodeManager getTreeNodeManager() {
		return treeNodeManager;
	}
	public void setTreeNodeManager(ITreeNodeManager treeNodeManager) {
		this.treeNodeManager = treeNodeManager;
	}

    protected IAdvContentFacetManager getAdvContentFacetManager() {
        return advContentFacetManager;
    }
    public void setAdvContentFacetManager(IAdvContentFacetManager advContentFacetManager) {
        this.advContentFacetManager = advContentFacetManager;
    }
    
}
