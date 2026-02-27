package org.foo.modules.cssoverride.filters;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Jahia RenderFilter that intercepts the final HTML output of a page
 * and appends a custom CSS override link just before the closing </head> tag.
 *
 * The filter applies only on full page renders (configuration "page")
 * so it processes the complete HTML document rather than individual components.
 */
@Component(service = RenderFilter.class, immediate = true)
public class CssOverrideFilter extends AbstractFilter {

    private static final Logger logger = LoggerFactory.getLogger(CssOverrideFilter.class);

    private static final String MODULE_NAME = "Jahia-CSS-override";

    // The override CSS path served from this module's src/main/resources/css/variables.css
    private static final String OVERRIDE_CSS_PATH = "/modules/" + MODULE_NAME + "/css/variables.css";

    private static final String LINK_TAG =
            "<jahia:resource type=\"css\" path=\"" + OVERRIDE_CSS_PATH.replace("/", "%2F") + "\"" +
            " insert=\"false\" resource=\"" + OVERRIDE_CSS_PATH + "\"" +
            " async=\"false\" defer=\"false\" title=\"\" key=\"\" />";

    public CssOverrideFilter() {
        setPriority(16);
        setApplyOnConfigurations("page");
        setApplyOnModes("live,preview");
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        // Let the full render chain complete first so we have the final HTML
        String out = super.execute(previousOut, renderContext, resource, chain);

        if (!renderContext.getSite().getInstalledModules().contains(MODULE_NAME)) {
            return out;
        }

        if (out != null) {
            int headClose = out.indexOf("</head>");
            if (headClose != -1) {
                logger.debug("CssOverrideFilter: appending '{}' before </head>", OVERRIDE_CSS_PATH);
                out = out.substring(0, headClose) + LINK_TAG + out.substring(headClose);
            }
        }

        return out;
    }
}
