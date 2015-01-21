/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
$(document).ready(function() {
    $("#aspect_artifactbrowser_CommunityBrowser_div_comunity-browser p.ds-paragraph:first-child").append("<div>"+
                                                                                                            "<a href=\"#\" class=\"expand-all btn btn-default btn-sm\">Expand All</a> "+
                                                                                                            "<a href=\"#\" class=\"close-all btn btn-default btn-sm\">Close All</a>"+
                                                                                                        "</div>");
    $(".toggler").click(function(e) {
        e.preventDefault();
        parentid = $(this).attr("data-target");
        $togglerNext = $(this).children(".hidden");
        $(this).children("i:not(.hidden)").addClass("hidden");
        $togglerNext.removeClass("hidden");
        if ($(parentid).hasClass("hidden")) {
            $(parentid).removeClass("hidden");
        } else {
            $(parentid).addClass("hidden");
        }
    });
    $(".expand-all").click(function(e) {
        e.preventDefault();
        $(".toggler").children().each(function() {
            $("i.closed-icon").addClass("hidden");
            $("i.open-icon").removeClass("hidden");
        });
        $(".sub-tree-wrapper").removeClass("hidden");
    });
    $(".close-all").click(function(e) {
        e.preventDefault();
        $(".toggler").children().each(function() {
            $("i.closed-icon").removeClass("hidden");
            $("i.open-icon").addClass("hidden");
        });
        $(".sub-tree-wrapper").addClass("hidden");
    });
});
