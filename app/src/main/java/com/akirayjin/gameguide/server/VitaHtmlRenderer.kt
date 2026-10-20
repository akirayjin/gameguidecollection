package com.akirayjin.gameguide.server

import com.akirayjin.gameguide.data.GameEntity
import com.akirayjin.gameguide.data.GameWithSections
import com.akirayjin.gameguide.data.GuideSectionEntity
import com.akirayjin.gameguide.data.Platforms

object VitaHtmlRenderer {

    fun getCss(): String {
        return """
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
                font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                background-color: #0d1117;
                color: #c9d1d9;
                line-height: 1.6;
                padding: 0;
                margin: 0;
                font-size: 16px;
                -webkit-text-size-adjust: 100%;
            }
            .header {
                background: linear-gradient(135deg, #0b2545 0%, #134074 100%);
                color: #ffffff;
                padding: 10px 14px;
                display: flex;
                align-items: center;
                justify-content: space-between;
                border-bottom: 2px solid #0072ce;
                box-shadow: 0 4px 12px rgba(0,0,0,0.5);
                position: sticky;
                top: 0;
                z-index: 100;
                flex-wrap: wrap;
                gap: 8px;
            }
            .brand {
                display: flex;
                align-items: center;
                gap: 8px;
                text-decoration: none;
                color: #ffffff;
            }
            .brand-logo {
                background: #0072ce;
                color: #fff;
                font-weight: bold;
                padding: 4px 8px;
                border-radius: 6px;
                font-size: 13px;
                letter-spacing: 1px;
            }
            .brand-title {
                font-size: 16px;
                font-weight: bold;
            }
            .header-controls {
                display: flex;
                gap: 6px;
                align-items: center;
                flex-wrap: wrap;
            }
            .btn-action {
                background: #21262d;
                color: #58a6ff;
                border: 1px solid #30363d;
                padding: 6px 10px;
                border-radius: 6px;
                cursor: pointer;
                font-size: 13px;
                text-decoration: none;
                white-space: nowrap;
            }
            .btn-action:hover, .btn-action:active {
                background: #30363d;
                color: #ffffff;
            }
            .search-form {
                display: flex;
                gap: 4px;
                margin: 0;
            }
            .search-input {
                background: #161b22;
                border: 1px solid #30363d;
                color: #f0f6fc;
                padding: 6px 10px;
                border-radius: 6px;
                font-size: 13px;
                width: 140px;
            }
            .container {
                max-width: 960px;
                margin: 0 auto;
                padding: 12px;
            }
            .game-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
                gap: 12px;
                margin-top: 12px;
            }
            .game-card {
                background: #161b22;
                border: 1px solid #30363d;
                border-radius: 8px;
                padding: 14px;
                transition: border-color 0.2s;
                text-decoration: none;
                color: inherit;
                display: block;
            }
            .game-card:hover, .game-card:active {
                border-color: #58a6ff;
                background: #1c2128;
            }
            .game-title {
                color: #58a6ff;
                font-size: 18px;
                font-weight: bold;
                margin-bottom: 6px;
            }
            .game-platform {
                display: inline-block;
                background: #0072ce;
                color: #ffffff;
                font-size: 11px;
                font-weight: bold;
                padding: 2px 8px;
                border-radius: 12px;
                margin-bottom: 6px;
            }
            .game-summary {
                color: #8b949e;
                font-size: 13px;
                line-height: 1.4;
            }
            .guide-layout {
                display: flex;
                gap: 16px;
                margin-top: 10px;
            }
            .sidebar {
                width: 260px;
                flex-shrink: 0;
                background: #161b22;
                border: 1px solid #30363d;
                border-radius: 8px;
                padding: 12px;
                height: fit-content;
            }
            .sidebar-title {
                font-size: 13px;
                text-transform: uppercase;
                letter-spacing: 1px;
                color: #8b949e;
                margin-bottom: 8px;
                padding-bottom: 4px;
                border-bottom: 1px solid #30363d;
            }
            .nav-list {
                list-style: none;
            }
            .nav-item {
                margin-bottom: 6px;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }
            .nav-link {
                display: block;
                padding: 6px 8px;
                color: #c9d1d9;
                text-decoration: none;
                border-radius: 6px;
                font-size: 13px;
                flex-grow: 1;
            }
            .nav-link:hover, .nav-link.active {
                background: #0072ce;
                color: #ffffff;
                font-weight: bold;
            }
            .main-content {
                flex-grow: 1;
                background: #161b22;
                border: 1px solid #30363d;
                border-radius: 8px;
                padding: 16px;
                min-width: 0;
                overflow-x: auto;
            }
            .content-area h1, .content-area h2, .content-area h3 {
                color: #58a6ff;
                margin-top: 16px;
                margin-bottom: 8px;
            }
            .content-area h1 { font-size: 22px; border-bottom: 1px solid #30363d; padding-bottom: 6px; }
            .content-area h2 { font-size: 18px; }
            .content-area h3 { font-size: 16px; }
            .content-area ul, .content-area ol {
                margin-left: 18px;
                margin-bottom: 12px;
            }
            .content-area li { margin-bottom: 4px; }
            .content-area table {
                width: 100%;
                border-collapse: collapse;
                margin: 12px 0;
                background: #0d1117;
                border-radius: 6px;
                overflow-x: auto;
                display: block;
            }
            .content-area th, .content-area td {
                padding: 8px 10px;
                border: 1px solid #30363d;
                text-align: left;
                font-size: 13px;
            }
            .content-area th {
                background: #21262d;
                color: #f0f6fc;
            }
            .content-area img {
                max-width: 100%;
                height: auto;
                border-radius: 6px;
                margin: 8px 0;
            }
            .note-box {
                background: #1f2d3d;
                border-left: 4px solid #388bfd;
                padding: 10px 14px;
                border-radius: 4px;
                margin: 12px 0;
                color: #e6edf3;
                font-size: 14px;
            }
            .warning-box {
                background: #3d2422;
                border-left: 4px solid #f85149;
                padding: 10px 14px;
                border-radius: 4px;
                margin: 12px 0;
                color: #ff7b72;
                font-size: 14px;
            }
            .nav-buttons {
                display: flex;
                justify-content: space-between;
                margin-top: 20px;
                padding-top: 12px;
                border-top: 1px solid #30363d;
                gap: 8px;
            }
            .form-group { margin-bottom: 12px; }
            .form-group label { display: block; margin-bottom: 4px; color: #58a6ff; font-weight: bold; }
            .form-control { width: 100%; background: #0d1117; border: 1px solid #30363d; color: #f0f6fc; padding: 8px; border-radius: 6px; font-size: 14px; }
            textarea.form-control { min-height: 250px; font-family: monospace; }
            
            /* Modal overlay */
            .modal-overlay {
                display: none;
                position: fixed;
                top: 0; left: 0; width: 100%; height: 100%;
                background: rgba(0, 0, 0, 0.7);
                z-index: 1000;
                justify-content: center;
                align-items: center;
            }
            .modal-content {
                background: #161b22;
                border: 1px solid #30363d;
                border-radius: 10px;
                padding: 20px;
                width: 90%;
                max-width: 500px;
                box-shadow: 0 8px 24px rgba(0,0,0,0.6);
            }

            @media (max-width: 768px) {
                .guide-layout { flex-direction: column; }
                .sidebar { width: 100%; margin-bottom: 12px; }
                .header { padding: 8px 10px; }
                .brand-title { font-size: 14px; }
                .search-input { width: 110px; }
                .container { padding: 8px; }
                .main-content { padding: 12px; }
            }
        """.trimIndent()
    }

    fun getJs(): String {
        return """
            function setFontSize(change) {
                var body = document.body;
                var currentSize = parseInt(window.getComputedStyle(body).fontSize);
                var newSize = currentSize + change;
                if (newSize >= 12 && newSize <= 24) {
                    body.style.fontSize = newSize + 'px';
                }
            }
        """.trimIndent()
    }

    fun renderHeader(title: String = "Game Guides", showAdmin: Boolean = true): String {
        val adminLink = if (showAdmin) """<a href="/admin" class="btn-action" style="background: #0072ce; color: #fff; font-weight: bold;">Admin</a>""" else ""
        return """
            <div class="header">
                <a href="/" class="brand">
                    <span class="brand-logo">GUIDE</span>
                    <span class="brand-title">$title</span>
                </a>
                <div class="header-controls">
                    $adminLink
                    <form action="/search" method="get" class="search-form">
                        <input type="text" name="q" placeholder="Search..." class="search-input" />
                        <button type="submit" class="btn-action">Search</button>
                    </form>
                    <button onclick="setFontSize(-2)" class="btn-action">A-</button>
                    <button onclick="setFontSize(2)" class="btn-action">A+</button>
                    <a href="/" class="btn-action">Home</a>
                </div>
            </div>
        """.trimIndent()
    }

    fun renderIndexPage(games: List<GameWithSections>, isDesktop: Boolean = true): String {
        val gameCards = if (games.isEmpty()) {
            """
                <div class="note-box" style="margin-top: 20px;">
                    <h3>No Game Guides Found</h3>
                    <p>Open the Game Guide app to add guides!</p>
                </div>
            """.trimIndent()
        } else {
            games.joinToString("\n") { g ->
                """
                    <a href="/game?id=${g.game.id}" class="game-card">
                        <span class="game-platform">${g.game.platform}</span>
                        <div class="game-title">${escapeHtml(g.game.title)}</div>
                        <div class="game-summary">${escapeHtml(g.game.summary)}</div>
                        <div style="margin-top: 8px; font-size: 12px; color: #58a6ff;">
                            ${g.sections.size} Section(s) Available &rarr;
                        </div>
                    </a>
                """.trimIndent()
            }
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes" />
                <title>Game Guides Hub</title>
                <link rel="stylesheet" href="/css/vita.css" />
                <script src="/js/vita.js"></script>
            </head>
            <body>
                ${renderHeader("Game Guide Hub", isDesktop)}
                <div class="container">
                    <h2 style="margin-top: 6px; color: #f0f6fc; font-size: 20px;">Available Game Guides</h2>
                    <p style="color: #8b949e; font-size: 13px;">Select a game below to browse walkthroughs and boss guides across all platforms.</p>
                    <div class="game-grid">
                        $gameCards
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun renderGamePage(gameWithSections: GameWithSections, selectedSectionId: Long?, isDesktop: Boolean = true): String {
        val game = gameWithSections.game
        val sections = gameWithSections.sections.sortedBy { it.orderIndex }
        val activeSection = sections.find { it.id == selectedSectionId } ?: sections.firstOrNull()

        val sidebarLinks = sections.joinToString("\n") { sec ->
            val activeClass = if (sec.id == activeSection?.id) "active" else ""
            """
                <li class="nav-item">
                    <a href="/game?id=${game.id}&sectionId=${sec.id}" class="nav-link $activeClass">
                        ${escapeHtml(sec.title)}
                    </a>
                </li>
            """.trimIndent()
        }

        val contentHtml = if (activeSection != null) {
            val currentIndex = sections.indexOf(activeSection)
            val prevSection = if (currentIndex > 0) sections[currentIndex - 1] else null
            val nextSection = if (currentIndex < sections.size - 1) sections[currentIndex + 1] else null

            val navButtons = """
                <div class="nav-buttons">
                    ${if (prevSection != null) "<a href=\"/game?id=${game.id}&sectionId=${prevSection.id}\" class=\"btn-action\">&larr; Prev</a>" else "<div></div>"}
                    ${if (nextSection != null) "<a href=\"/game?id=${game.id}&sectionId=${nextSection.id}\" class=\"btn-action\">Next &rarr;</a>" else "<div></div>"}
                </div>
            """.trimIndent()

            """
                <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #30363d; padding-bottom: 6px; margin-bottom: 12px; flex-wrap: wrap; gap: 8px;">
                    <h1>${escapeHtml(activeSection.title)}</h1>
                </div>
                <div class="content-area">
                    ${activeSection.content}
                </div>
                $navButtons
            """.trimIndent()
        } else {
            """
                <div class="note-box">
                    <h3>No Sections Available</h3>
                    <p>Add walkthrough sections via Desktop Admin.</p>
                </div>
            """.trimIndent()
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes" />
                <title>${escapeHtml(game.title)} - Guide</title>
                <link rel="stylesheet" href="/css/vita.css" />
                <script src="/js/vita.js"></script>
            </head>
            <body>
                ${renderHeader(game.title, isDesktop)}
                <div class="container">
                    <div style="margin-bottom: 10px; font-size: 13px; display: flex; justify-content: space-between; flex-wrap: wrap; gap: 6px;">
                        <a href="/" style="color: #58a6ff; text-decoration: none;">&larr; Back to Index</a>
                    </div>
                    <div class="guide-layout">
                        <div class="sidebar">
                            <div class="sidebar-title">Table of Contents</div>
                            <ul class="nav-list">
                                $sidebarLinks
                            </ul>
                        </div>
                        <div class="main-content">
                            $contentHtml
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun renderSearchResultsPage(query: String, results: List<GameWithSections>, isDesktop: Boolean = true): String {
        val resultHtml = if (results.isEmpty()) {
            """
                <div class="note-box">
                    <h3>No results found for "$query"</h3>
                    <p>Try searching for a different keyword.</p>
                </div>
            """.trimIndent()
        } else {
            results.joinToString("\n") { g ->
                val matchingSections = g.sections.filter { 
                    it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
                }
                val sectionLinks = matchingSections.joinToString(", ") { sec ->
                    """<a href="/game?id=${g.game.id}&sectionId=${sec.id}" style="color: #58a6ff;">${escapeHtml(sec.title)}</a>"""
                }

                """
                    <div class="game-card" style="margin-bottom: 10px;">
                        <div class="game-title">
                            <a href="/game?id=${g.game.id}" style="color: #58a6ff; text-decoration: none;">
                                ${escapeHtml(g.game.title)}
                            </a>
                        </div>
                        <div class="game-summary">${escapeHtml(g.game.summary)}</div>
                        ${if (matchingSections.isNotEmpty()) "<div style=\"margin-top: 6px; font-size: 12px;\">Matched Sections: $sectionLinks</div>" else ""}
                    </div>
                """.trimIndent()
            }
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes" />
                <title>Search: ${escapeHtml(query)}</title>
                <link rel="stylesheet" href="/css/vita.css" />
                <script src="/js/vita.js"></script>
            </head>
            <body>
                ${renderHeader("Search Results", isDesktop)}
                <div class="container">
                    <h2 style="margin-top: 6px; color: #f0f6fc; font-size: 20px;">Search Results for "${escapeHtml(query)}"</h2>
                    <div style="margin-top: 12px;">
                        $resultHtml
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun renderAdminDashboard(games: List<GameWithSections>): String {
        val gamesJsonMapStr = buildString {
            append("{")
            games.forEachIndexed { index, g ->
                if (index > 0) append(",")
                append("\"${g.game.id}\":[")
                g.sections.sortedBy { it.orderIndex }.forEachIndexed { sIdx, s ->
                    if (sIdx > 0) append(",")
                    val safeTitle = s.title.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
                    append("{\"id\":${s.id},\"title\":\"$safeTitle\"}")
                }
                append("]")
            }
            append("}")
        }

        val rows = games.joinToString("\n") { g ->
            val sectionsList = g.sections.sortedBy { it.orderIndex }.joinToString("<br>") { sec ->
                "- ${escapeHtml(sec.title)}"
            }
            val reorderBtn = if (g.sections.size > 1) {
                """<button class="btn-action" onclick="openReorderModal(${g.game.id}, '${escapeHtml(g.game.title).replace("'", "\\'")}')">Reorder</button>"""
            } else ""

            """
                <tr style="border-bottom: 1px solid #30363d;">
                    <td style="padding: 10px; font-weight: bold; color: #58a6ff;">${escapeHtml(g.game.title)}</td>
                    <td style="padding: 10px;">${escapeHtml(g.game.platform)}</td>
                    <td style="padding: 10px; font-size: 13px;">$sectionsList</td>
                    <td style="padding: 10px; white-space: nowrap;">
                        $reorderBtn
                        <a href="/admin/new-section?gameId=${g.game.id}" class="btn-action">+ Section</a>
                        <a href="/admin/edit-game?gameId=${g.game.id}" class="btn-action">Edit Game</a>
                        <a href="/admin/export-game?gameId=${g.game.id}" class="btn-action">Export</a>
                        <a href="/admin/delete-game?gameId=${g.game.id}" class="btn-action" style="color: #f85149;" onclick="return confirm('Delete game and all sections?');">Delete</a>
                    </td>
                </tr>
            """.trimIndent()
        }

        val platformOptions = Platforms.ALL.joinToString("\n") { p ->
            "<option value=\"$p\">$p</option>"
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes" />
                <title>Admin Dashboard</title>
                <link rel="stylesheet" href="/css/vita.css" />
            </head>
            <body>
                ${renderHeader("Admin Dashboard")}
                <div class="container">
                    <h2 style="margin-top: 6px; color: #f0f6fc; font-size: 20px;">Manage Game Guides</h2>
                    <p style="color: #8b949e; font-size: 13px; margin-bottom: 12px;">Add guides and copy/paste walkthrough content across all gaming platforms.</p>
                    
                    <div style="background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 14px; margin-bottom: 16px;">
                        <h3 style="color: #58a6ff; margin-bottom: 8px; font-size: 16px;">Add New Game Guide</h3>
                        <form action="/admin/save-game" method="post" onsubmit="return confirm('Create new game guide?');">
                            <div class="form-group">
                                <label>Game Title</label>
                                <input type="text" name="title" class="form-control" required placeholder="e.g. The Legend of Zelda" />
                            </div>
                            <div class="form-group">
                                <label>Platform</label>
                                <select name="platform" class="form-control">
                                    $platformOptions
                                </select>
                            </div>
                            <div class="form-group">
                                <label>Summary / Notes</label>
                                <input type="text" name="summary" class="form-control" placeholder="Short description..." />
                            </div>
                            <button type="submit" class="btn-action" style="background: #238636; color: #fff; font-weight: bold;" onclick="return confirm('Save this game guide?');">Create Game</button>
                        </form>
                    </div>

                    <div style="background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 14px; overflow-x: auto;">
                        <h3 style="color: #58a6ff; margin-bottom: 8px; font-size: 16px;">Existing Guides</h3>
                        <table style="width: 100%; border-collapse: collapse; min-width: 600px;">
                            <thead>
                                <tr style="background: #21262d; border-bottom: 2px solid #30363d;">
                                    <th style="padding: 8px; text-align: left;">Game Title</th>
                                    <th style="padding: 8px; text-align: left;">Platform</th>
                                    <th style="padding: 8px; text-align: left;">Sections</th>
                                    <th style="padding: 8px; text-align: left;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${rows.ifBlank { "<tr><td colspan='4' style='padding: 16px; text-align: center; color: #8b949e;'>No guides available. Create one above!</td></tr>" }}
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Reorder Modal Popup -->
                <div id="reorderModal" class="modal-overlay">
                    <div class="modal-content">
                        <h3 id="modalGameTitle" style="color: #58a6ff; margin-bottom: 8px; font-size: 18px;">Reorder Sections</h3>
                        <p style="color: #8b949e; font-size: 13px; margin-bottom: 12px;">Use Up (▲) and Down (▼) buttons to reorder sections instantly without page reload:</p>
                        <div id="modalSectionsList" style="max-height: 250px; overflow-y: auto; margin-bottom: 16px; display: flex; flex-direction: column; gap: 6px;"></div>
                        <div style="display: flex; justify-content: flex-end;">
                            <button type="button" class="btn-action" style="background: #0072ce; color: #fff; font-weight: bold;" onclick="closeReorderModal()">Done</button>
                        </div>
                    </div>
                </div>

                <script>
                var gamesData = $gamesJsonMapStr;

                function openReorderModal(gameId, gameTitle) {
                    document.getElementById('modalGameTitle').innerText = 'Reorder Sections: ' + gameTitle;
                    renderModalSections(gameId);
                    document.getElementById('reorderModal').style.display = 'flex';
                }

                function closeReorderModal() {
                    document.getElementById('reorderModal').style.display = 'none';
                    location.reload();
                }

                function renderModalSections(gameId) {
                    var sections = gamesData[gameId] || gamesData[String(gameId)] || [];
                    var container = document.getElementById('modalSectionsList');
                    container.innerHTML = '';
                    if (sections.length === 0) {
                        container.innerHTML = '<div style="color: #8b949e; font-size: 13px; text-align: center; padding: 12px;">No sections available.</div>';
                        return;
                    }
                    sections.forEach(function(sec, idx) {
                        var row = document.createElement('div');
                        row.style.cssText = "display: flex; justify-content: space-between; align-items: center; background: #0d1117; border: 1px solid #30363d; border-radius: 6px; padding: 8px 12px;";
                        
                        var titleSpan = document.createElement('span');
                        titleSpan.style.cssText = "font-size: 14px; font-weight: 500; color: #f0f6fc;";
                        titleSpan.innerText = (idx + 1) + '. ' + sec.title;
                        row.appendChild(titleSpan);

                        var btnGroup = document.createElement('div');
                        btnGroup.style.cssText = "display: flex; gap: 4px;";

                        var upBtn = document.createElement('button');
                        upBtn.className = 'btn-action';
                        upBtn.innerText = '▲';
                        upBtn.disabled = (idx === 0);
                        upBtn.style.opacity = (idx === 0) ? '0.5' : '1';
                        upBtn.onclick = function() { moveSecLocal(gameId, idx, 'up'); };
                        btnGroup.appendChild(upBtn);

                        var downBtn = document.createElement('button');
                        downBtn.className = 'btn-action';
                        downBtn.innerText = '▼';
                        downBtn.disabled = (idx === sections.length - 1);
                        downBtn.style.opacity = (idx === sections.length - 1) ? '0.5' : '1';
                        downBtn.onclick = function() { moveSecLocal(gameId, idx, 'down'); };
                        btnGroup.appendChild(downBtn);

                        row.appendChild(btnGroup);
                        container.appendChild(row);
                    });
                }

                function moveSecLocal(gameId, idx, direction) {
                    var sections = gamesData[gameId] || gamesData[String(gameId)];
                    var targetIdx = (direction === 'up') ? idx - 1 : idx + 1;
                    if (!sections || targetIdx < 0 || targetIdx >= sections.length) return;

                    var temp = sections[idx];
                    sections[idx] = sections[targetIdx];
                    sections[targetIdx] = temp;

                    fetch('/admin/move-section?gameId=' + gameId + '&sectionId=' + sections[targetIdx].id + '&direction=' + direction)
                        .then(function() {
                            renderModalSections(gameId);
                        })
                        .catch(function(err) {
                            console.error(err);
                        });
                }
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    fun renderAdminEditGame(game: GameEntity?): String {
        val gId = game?.id ?: 0L
        val gTitle = game?.title ?: ""
        val gPlatform = game?.platform ?: "PS Vita"
        val gSummary = game?.summary ?: ""
        val heading = if (game == null) "Add Game Guide" else "Edit Game Guide"

        val platformOptions = Platforms.ALL.joinToString("\n") { p ->
            val selected = if (p == gPlatform) "selected" else ""
            "<option value=\"$p\" $selected>$p</option>"
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes" />
                <title>$heading - Admin</title>
                <link rel="stylesheet" href="/css/vita.css" />
            </head>
            <body>
                ${renderHeader("Desktop Admin")}
                <div class="container">
                    <div style="background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; margin-top: 10px;">
                        <h2 style="color: #58a6ff; margin-bottom: 12px; font-size: 20px;">$heading</h2>
                        <form action="/admin/save-game?gameId=$gId" method="post" onsubmit="return confirm('Save game details?');">
                            <input type="hidden" name="gameId" value="$gId" />
                            <div class="form-group">
                                <label>Game Title</label>
                                <input type="text" name="title" class="form-control" value="${escapeHtml(gTitle)}" required />
                            </div>
                            <div class="form-group">
                                <label>Platform</label>
                                <select name="platform" class="form-control">
                                    $platformOptions
                                </select>
                            </div>
                            <div class="form-group">
                                <label>Summary / Notes</label>
                                <input type="text" name="summary" class="form-control" value="${escapeHtml(gSummary)}" />
                            </div>
                            <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                                <button type="submit" class="btn-action" style="background: #238636; color: #fff; font-weight: bold;" onclick="return confirm('Save changes to game?');">Save Game</button>
                                ${if (gId > 0L) "<a href=\"/admin/delete-game?gameId=$gId\" class=\"btn-action\" style=\"color: #f85149;\" onclick=\"return confirm('Delete this game guide and all sections?');\">Delete Game Guide</a>" else ""}
                                <a href="/admin" class="btn-action">Cancel</a>
                            </div>
                        </form>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun renderAdminEditSection(gameId: Long, section: GuideSectionEntity?): String {
        val sId = section?.id ?: 0L
        val sTitle = section?.title ?: ""
        val sContent = section?.content ?: ""
        val heading = if (section == null) "Add New Section" else "Edit Section"

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes" />
                <title>$heading - Admin</title>
                <link rel="stylesheet" href="/css/vita.css" />
            </head>
            <body>
                ${renderHeader("Desktop Admin")}
                <div class="container">
                    <div style="background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px; margin-top: 10px;">
                        <h2 style="color: #58a6ff; margin-bottom: 12px; font-size: 20px;">$heading</h2>
                        <form action="/admin/save-section?gameId=$gameId&sectionId=$sId" method="post" onsubmit="return confirm('Save section changes?');">
                            <input type="hidden" name="gameId" value="$gameId" />
                            <input type="hidden" name="sectionId" value="$sId" />
                            <div class="form-group">
                                <label>Section Title</label>
                                <input type="text" name="title" class="form-control" value="${escapeHtml(sTitle)}" required />
                            </div>
                            <div class="form-group">
                                <label>HTML Content</label>
                                <div style="display: flex; gap: 4px; flex-wrap: wrap; margin-bottom: 6px;">
                                    <button type="button" class="btn-action" onclick="insertTag('<h1>', '</h1>')">H1</button>
                                    <button type="button" class="btn-action" onclick="insertTag('<h2>', '</h2>')">H2</button>
                                    <button type="button" class="btn-action" onclick="insertTag('<h3>', '</h3>')">H3</button>
                                    <button type="button" class="btn-action" onclick="insertTag('<b>', '</b>')">Bold</button>
                                    <button type="button" class="btn-action" onclick="insertTag('<i>', '</i>')">Italic</button>
                                    <button type="button" class="btn-action" onclick="insertTag('<code>', '</code>')">Code</button>
                                    <button type="button" class="btn-action" onclick="insertTag('<ul>\n  <li>', '</li>\n</ul>')">List</button>
                                    <button type="button" class="btn-action" onclick="insertTag('<table>\n  <tr><th>Header</th></tr>\n  <tr><td>Data</td></tr>\n</table>', '')">Table</button>
                                    <button type="button" class="btn-action" onclick="insertTag('<div class=\"note-box\">\n  <p>', '</p>\n</div>')">Note</button>
                                    <button type="button" class="btn-action" onclick="insertTag('<div class=\"warning-box\">\n  <p>', '</p>\n</div>')">Warning</button>
                                    <button type="button" class="btn-action" onclick="insertTag('<img src=\"https://...\" />', '')">Image</button>
                                </div>
                                <textarea name="content" class="form-control">${escapeHtml(sContent)}</textarea>
                            </div>
                            <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                                <button type="submit" class="btn-action" style="background: #238636; color: #fff; font-weight: bold;" onclick="return confirm('Save this section?');">Save Section</button>
                                ${if (sId > 0L) "<a href=\"/admin/delete-section?gameId=$gameId&sectionId=$sId\" class=\"btn-action\" style=\"color: #f85149;\" onclick=\"return confirm('Delete this section?');\">Delete Section</a>" else ""}
                                <a href="/admin" class="btn-action">Cancel</a>
                            </div>
                        </form>
                    </div>
                </div>
                <script>
                function insertTag(startTag, endTag) {
                    var ta = document.querySelector('textarea[name=content]');
                    var start = ta.selectionStart;
                    var end = ta.selectionEnd;
                    var val = ta.value;
                    ta.value = val.substring(0, start) + startTag + val.substring(start, end) + endTag + val.substring(end);
                    ta.focus();
                    ta.selectionStart = start + startTag.length;
                    ta.selectionEnd = end + startTag.length;
                }
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
