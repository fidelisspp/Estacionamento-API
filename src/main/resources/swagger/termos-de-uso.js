(function () {
  var REPOSITORIO = "https://github.com/fidelisspp/Estacionamento-API/blob/main/";
  var modal = null;

  function escapar(texto) {
    return texto
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function endereco(url) {
    return /^https?:\/\//.test(url) ? url : REPOSITORIO + url;
  }

  function emLinha(texto) {
    var codigos = [];
    var html = escapar(texto).replace(/`([^`]+)`/g, function (_, codigo) {
      codigos.push(codigo);
      return "\u0000" + (codigos.length - 1) + "\u0000";
    });
    html = html
      .replace(/\[([^\]]+)\]\(([^)]+)\)/g, function (_, rotulo, url) {
        return '<a href="' + endereco(url) + '" target="_blank" rel="noopener noreferrer">' + rotulo + "</a>";
      })
      .replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>")
      .replace(/\*([^*]+)\*/g, "<em>$1</em>");
    return html.replace(/\u0000(\d+)\u0000/g, function (_, i) {
      return "<code>" + codigos[i] + "</code>";
    });
  }

  function celulas(linha) {
    return linha.replace(/^\||\|$/g, "").split("|").map(function (c) {
      return c.trim();
    });
  }

  function paraHtml(markdown) {
    var linhas = markdown.replace(/\r/g, "").split("\n");
    var html = [];
    var i = 0;

    function coletar(teste) {
      var bloco = [];
      while (i < linhas.length && teste(linhas[i])) {
        bloco.push(linhas[i]);
        i++;
      }
      return bloco;
    }

    while (i < linhas.length) {
      var linha = linhas[i];
      var titulo = linha.match(/^(#{1,6})\s+(.*)$/);

      if (!linha.trim()) {
        i++;
      } else if (titulo) {
        var nivel = titulo[1].length;
        html.push("<h" + nivel + ">" + emLinha(titulo[2]) + "</h" + nivel + ">");
        i++;
      } else if (/^\|/.test(linha)) {
        var tabela = coletar(function (l) { return /^\|/.test(l); });
        var cabecalho = celulas(tabela[0]).map(function (c) { return "<th>" + emLinha(c) + "</th>"; }).join("");
        var corpo = tabela.slice(2).map(function (l) {
          return "<tr>" + celulas(l).map(function (c) { return "<td>" + emLinha(c) + "</td>"; }).join("") + "</tr>";
        }).join("");
        html.push("<table><thead><tr>" + cabecalho + "</tr></thead><tbody>" + corpo + "</tbody></table>");
      } else if (/^>\s?/.test(linha)) {
        var citacao = coletar(function (l) { return /^>\s?/.test(l); }).map(function (l) {
          return emLinha(l.replace(/^>\s?/, ""));
        });
        html.push("<blockquote><p>" + citacao.join("<br>") + "</p></blockquote>");
      } else if (/^[-*]\s+/.test(linha)) {
        var itens = coletar(function (l) { return /^[-*]\s+/.test(l); }).map(function (l) {
          return "<li>" + emLinha(l.replace(/^[-*]\s+/, "")) + "</li>";
        });
        html.push("<ul>" + itens.join("") + "</ul>");
      } else {
        var paragrafo = coletar(function (l) {
          return l.trim() && !/^(#{1,6}\s|\||>|[-*]\s)/.test(l);
        });
        html.push("<p>" + paragrafo.map(emLinha).join("<br>") + "</p>");
      }
    }
    return html.join("\n");
  }

  function adicionarEstilo() {
    var estilo = document.createElement("style");
    estilo.textContent = [
      ".termos-fundo{position:fixed;inset:0;z-index:10000;display:flex;align-items:center;justify-content:center;padding:16px;background:rgba(0,0,0,.6)}",
      ".termos-janela{display:flex;flex-direction:column;width:100%;max-width:820px;max-height:90vh;background:#fff;color:#3b4151;border-radius:6px;box-shadow:0 10px 40px rgba(0,0,0,.35);font-family:sans-serif}",
      ".termos-topo{display:flex;align-items:center;justify-content:space-between;gap:16px;padding:14px 20px;border-bottom:1px solid #e0e0e0}",
      ".termos-topo h2{margin:0;font-size:18px}",
      ".termos-fechar{border:0;background:none;font-size:26px;line-height:1;cursor:pointer;color:inherit;padding:0 4px}",
      ".termos-conteudo{overflow-y:auto;padding:8px 24px 24px;font-size:14px;line-height:1.6}",
      ".termos-conteudo h1{display:none}",
      ".termos-conteudo h2{font-size:17px;margin:24px 0 8px}",
      ".termos-conteudo blockquote{margin:12px 0;padding:8px 14px;border-left:4px solid #49cc90;background:#f3fbf7}",
      ".termos-conteudo blockquote p{margin:0}",
      ".termos-conteudo table{border-collapse:collapse;width:100%;margin:12px 0}",
      ".termos-conteudo th,.termos-conteudo td{border:1px solid #d9d9d9;padding:6px 10px;text-align:left}",
      ".termos-conteudo th{background:#f5f5f5}",
      ".termos-conteudo code{background:#f0f0f0;padding:1px 5px;border-radius:3px;font-size:13px}",
      ".termos-conteudo a{color:#4990e2}"
    ].join("\n");
    document.head.appendChild(estilo);
  }

  function fechar() {
    if (modal) {
      modal.style.display = "none";
      document.body.style.overflow = "";
    }
  }

  function abrir() {
    if (!modal) {
      adicionarEstilo();
      modal = document.createElement("div");
      modal.className = "termos-fundo";
      modal.innerHTML =
        '<div class="termos-janela" role="dialog" aria-modal="true" aria-labelledby="termos-titulo">' +
        '<div class="termos-topo"><h2 id="termos-titulo">Termos de Uso</h2>' +
        '<button type="button" class="termos-fechar" aria-label="Fechar">&times;</button></div>' +
        '<div class="termos-conteudo">' + paraHtml(window.TERMOS_DE_USO || "") + "</div></div>";
      modal.addEventListener("click", function (evento) {
        if (evento.target === modal || evento.target.closest(".termos-fechar")) {
          fechar();
        }
      });
      document.body.appendChild(modal);
    }
    modal.style.display = "flex";
    document.body.style.overflow = "hidden";
    modal.querySelector(".termos-fechar").focus();
  }

  document.addEventListener("click", function (evento) {
    if (evento.target.closest && evento.target.closest(".info__tos a")) {
      evento.preventDefault();
      abrir();
    }
  }, true);

  document.addEventListener("keydown", function (evento) {
    if (evento.key === "Escape") {
      fechar();
    }
  });
})();
