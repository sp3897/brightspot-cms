package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.psddev.cms.db.PageFilter;
import com.psddev.cms.db.Schedule;
import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.AuthenticationFilter;
import com.psddev.cms.tool.CmsTool;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "inlineEditor")
public class InlineEditor extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ToolUser user = AuthenticationFilter.Static.getInsecureToolUser(request);

        if (user == null) {
            throw new IllegalStateException();
        }

        @SuppressWarnings("resource")
        ToolPageContext page = new ToolPageContext(getServletContext(), request, response);
        UUID mainId = page.param(UUID.class, "id");
        Object mainObject = Query.
                fromAll().
                where("_id = ?", mainId).
                first();

        if (mainObject == null) {
            throw new IllegalArgumentException(String.format(
                    "No object with ID! [%s]",
                    mainId));
        }

        Schedule currentSchedule = user.getCurrentSchedule();

        page.writeHeader(null, false);
            page.writeStart("script", "type", "text/javascript");
                page.writeRaw("var CONTEXT_PATH = '");
                page.writeRaw(page.js(page.fullyQualifiedToolUrl(CmsTool.class, "/")));
                page.writeRaw("';");
            page.writeEnd();

            page.writeStart("style", "type", "text/css");
                page.writeCss(".toolBroadcast, .toolHeader, .toolFooter", "display", "none");
                page.writeCss("body, .toolContent", "background", "transparent");
                page.writeCss("body", "margin-top", "75px !important");
                page.writeCss(".toolContent", "position", "static");
            page.writeEnd();

            page.writeStart("ul", "class", "inlineEditorControls inlineEditorControls-main");
                page.writeStart("li", "class", "inlineEditorLogo");
                    page.writeStart("a",
                            "target", "_blank",
                            "href", page.fullyQualifiedToolUrl(CmsTool.class, "/"));
                        page.writeElement("img",
                                "src", page.cmsUrl("/style/brightspot.png"),
                                "alt", "Brightspot",
                                "width", 104,
                                "height", 14);
                    page.writeEnd();
                page.writeEnd();

                if (currentSchedule != null) {
                    page.writeStart("li");
                        page.writeStart("a",
                                "class", "icon icon-action-schedule",
                                "target", "_blank",
                                "href", page.fullyQualifiedToolUrl(CmsTool.class, "/scheduleEdit", "id", currentSchedule.getId()));
                            page.writeHtml("Current Schedule: ");
                            page.writeObjectLabel(currentSchedule);
                        page.writeEnd();
                    page.writeEnd();
                }

                page.writeStart("li");
                    page.writeStart("a",
                            "class", "icon icon-action-edit",
                            "target", "_blank",
                            "href", page.fullyQualifiedToolUrl(CmsTool.class, "/content/edit.jsp", "id", State.getInstance(mainObject).getId()));
                        page.writeTypeObjectLabel(mainObject);
                    page.writeEnd();
                page.writeEnd();

                page.writeStart("li");
                    page.writeStart("a",
                            "class", "icon icon-action-delete icon-only",
                            "style", page.cssString("color", "#ff0e40"),
                            "onclick", "$(window.parent.document.body).find('.cms-inlineEditor').remove(); return false;");
                        page.writeHtml("Remove");
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();

            if (PageFilter.Static.isInlineEditingAllContents(page.getRequest())) {
                page.writeStart("script",
                        "type", "text/javascript",
                        "src", page.cmsUrl("/script/inlineeditor.js"));
                page.writeEnd();
            }
        page.writeFooter();
    }
}
