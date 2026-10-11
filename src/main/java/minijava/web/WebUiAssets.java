package minijava.web;

/**
 * Embedded single-page application assets (HTML5, CSS3, JavaScript) for the
 * MiniJava Optimizer interactive web frontend.
 */
public class WebUiAssets {

    public static final String HTML_PAGE = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>MiniJava Optimizer — Compiler Optimization Explorer</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;500;600&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg: #090d16;
      --card-bg: rgba(17, 24, 39, 0.75);
      --card-border: rgba(255, 255, 255, 0.08);
      --card-hover: rgba(31, 41, 55, 0.85);
      --primary: #6366f1;
      --primary-hover: #4f46e5;
      --accent: #06b6d4;
      --success: #10b981;
      --warning: #f59e0b;
      --danger: #ef4444;
      --text: #f3f4f6;
      --text-muted: #9ca3af;
      --code-bg: #030712;
      --radius: 12px;
    }

    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Inter', -apple-system, sans-serif;
      background: radial-gradient(circle at 15% 15%, #131b2e 0%, var(--bg) 60%);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    header {
      padding: 16px 28px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: rgba(9, 13, 22, 0.85);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--card-border);
      position: sticky;
      top: 0;
      z-index: 50;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .brand-logo {
      width: 36px;
      height: 36px;
      background: linear-gradient(135deg, var(--primary), var(--accent));
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-family: 'Fira Code', monospace;
      color: #fff;
      box-shadow: 0 0 16px rgba(99, 102, 241, 0.4);
    }
    .brand-title {
      font-size: 1.15rem;
      font-weight: 700;
      letter-spacing: -0.02em;
      background: linear-gradient(90deg, #fff, #cbd5e1);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    .brand-subtitle {
      font-size: 0.75rem;
      color: var(--accent);
      font-weight: 500;
      letter-spacing: 0.05em;
      text-transform: uppercase;
    }

    .controls {
      display: flex;
      gap: 12px;
      align-items: center;
    }

    select, button {
      font-family: inherit;
      font-size: 0.85rem;
      border-radius: 8px;
      padding: 8px 14px;
      border: 1px solid var(--card-border);
      background: rgba(31, 41, 55, 0.8);
      color: var(--text);
      cursor: pointer;
      transition: all 0.2s ease;
    }
    select:focus, button:focus {
      outline: 2px solid var(--primary);
    }
    button.btn-primary {
      background: linear-gradient(135deg, var(--primary), #4f46e5);
      border-color: rgba(99, 102, 241, 0.5);
      color: #fff;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 6px;
      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
    }
    button.btn-primary:hover {
      box-shadow: 0 6px 16px rgba(99, 102, 241, 0.5);
      transform: translateY(-1px);
    }

    main {
      flex: 1;
      padding: 24px 28px;
      display: grid;
      grid-template-columns: 1fr 1.35fr;
      gap: 24px;
      max-width: 1700px;
      margin: 0 auto;
      width: 100%;
    }

    @media (max-width: 1024px) {
      main { grid-template-columns: 1fr; }
    }

    .card {
      background: var(--card-bg);
      backdrop-filter: blur(16px);
      border: 1px solid var(--card-border);
      border-radius: var(--radius);
      display: flex;
      flex-direction: column;
      overflow: hidden;
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
    }

    .card-header {
      padding: 14px 18px;
      border-bottom: 1px solid var(--card-border);
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: rgba(15, 23, 42, 0.5);
    }
    .card-title {
      font-size: 0.88rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: #94a3b8;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .editor-wrapper {
      position: relative;
      flex: 1;
      display: flex;
      min-height: 380px;
    }
    textarea.code-editor {
      width: 100%;
      height: 100%;
      background: var(--code-bg);
      border: none;
      color: #e2e8f0;
      font-family: 'Fira Code', monospace;
      font-size: 0.95rem;
      line-height: 1.6;
      padding: 16px;
      resize: none;
      outline: none;
      tab-size: 4;
    }

    .metrics-banner {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;
      margin-bottom: 16px;
    }
    .metric-pill {
      background: rgba(15, 23, 42, 0.6);
      border: 1px solid var(--card-border);
      padding: 10px 14px;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      gap: 2px;
    }
    .metric-label {
      font-size: 0.72rem;
      text-transform: uppercase;
      color: #94a3b8;
      font-weight: 600;
    }
    .metric-value {
      font-size: 1.25rem;
      font-weight: 700;
      font-family: 'Fira Code', monospace;
      color: #fff;
    }
    .metric-value.highlight {
      color: var(--success);
      text-shadow: 0 0 12px rgba(16, 185, 129, 0.4);
    }

    .tabs {
      display: flex;
      background: rgba(15, 23, 42, 0.7);
      border-bottom: 1px solid var(--card-border);
      padding: 4px 6px;
      gap: 4px;
    }
    .tab-btn {
      background: transparent;
      border: none;
      padding: 8px 14px;
      font-size: 0.8rem;
      font-weight: 600;
      color: #94a3b8;
      border-radius: 6px;
      cursor: pointer;
    }
    .tab-btn.active {
      background: var(--primary);
      color: #fff;
      box-shadow: 0 2px 8px rgba(99, 102, 241, 0.4);
    }

    .tab-content {
      flex: 1;
      padding: 16px;
      display: none;
      overflow-y: auto;
      max-height: 580px;
    }
    .tab-content.active { display: block; }

    pre.code-block {
      font-family: 'Fira Code', monospace;
      font-size: 0.88rem;
      line-height: 1.6;
      background: var(--code-bg);
      padding: 14px;
      border-radius: 8px;
      border: 1px solid rgba(255, 255, 255, 0.05);
      color: #e2e8f0;
      overflow-x: auto;
      white-space: pre;
    }

    .diff-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    .diff-col-title {
      font-size: 0.8rem;
      font-weight: 600;
      text-transform: uppercase;
      color: #94a3b8;
      margin-bottom: 8px;
      display: flex;
      justify-content: space-between;
    }

    .stepper-controls {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 14px;
      background: rgba(15, 23, 42, 0.6);
      padding: 8px 12px;
      border-radius: 8px;
      border: 1px solid var(--card-border);
    }
    .stepper-info {
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--accent);
      font-family: 'Fira Code', monospace;
    }

    .logs-list {
      list-style: none;
      margin-top: 10px;
    }
    .logs-list li {
      font-size: 0.82rem;
      font-family: 'Fira Code', monospace;
      padding: 4px 8px;
      background: rgba(99, 102, 241, 0.1);
      border-left: 3px solid var(--primary);
      margin-bottom: 4px;
      border-radius: 0 4px 4px 0;
      color: #c7d2fe;
    }

    .status-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    .badge-success {
      background: rgba(16, 185, 129, 0.15);
      color: #34d399;
      border: 1px solid rgba(16, 185, 129, 0.3);
    }
    .badge-danger {
      background: rgba(239, 68, 68, 0.15);
      color: #f87171;
      border: 1px solid rgba(239, 68, 68, 0.3);
    }

    .output-box {
      background: #000;
      border: 1px solid rgba(255, 255, 255, 0.1);
      padding: 12px 16px;
      border-radius: 8px;
      font-family: 'Fira Code', monospace;
      font-size: 0.95rem;
      color: #38bdf8;
      margin-top: 10px;
    }

    table.data-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.82rem;
      font-family: 'Fira Code', monospace;
    }
    table.data-table th, table.data-table td {
      padding: 8px 12px;
      text-align: left;
      border-bottom: 1px solid rgba(255, 255, 255, 0.06);
    }
    table.data-table th {
      background: rgba(15, 23, 42, 0.8);
      color: #94a3b8;
      font-weight: 600;
    }
  </style>
</head>
<body>
  <header>
    <div class="brand">
      <div class="brand-logo">ZX</div>
      <div>
        <div class="brand-title">ZYNTAX</div>
        <div class="brand-subtitle">Code Optimization Engine</div>
      </div>
    </div>
    <div class="controls">
      <select id="exampleSelector" title="Select Example Program">
        <option value="">Load Example Program...</option>
      </select>
      <select id="passSelector">
        <option value="all">All Optimization Passes (Pipeline to Fixpoint)</option>
        <option value="cf">Constant Folding Only</option>
        <option value="cp">Constant Propagation Only</option>
        <option value="as">Algebraic Simplification Only</option>
        <option value="dce">Dead Code Elimination Only</option>
      </select>
      <button id="runBtn" class="btn-primary">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
        Optimize & Run
      </button>
    </div>
  </header>

  <main>
    <!-- Left Column: Source Code Editor -->
    <div class="card">
      <div class="card-header">
        <span class="card-title">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 18 22 12 16 6"></polyline><polyline points="8 6 2 12 8 18"></polyline></svg>
          MiniJava Source Editor
        </span>
        <span id="charCount" style="font-size: 0.75rem; color: #64748b;">0 chars</span>
      </div>
      <div class="editor-wrapper">
        <textarea id="codeEditor" class="code-editor" spellcheck="false">// MiniJava Program
int a = 2 + 3;
int b = a * 4;
print(b);</textarea>
      </div>
      <div style="padding: 12px 16px; background: rgba(15, 23, 42, 0.5); border-top: 1px solid var(--card-border); font-size: 0.78rem; color: #94a3b8;">
        Keyboard shortcut: Press <kbd style="background: #1f2937; padding: 2px 6px; border-radius: 4px; border: 1px solid rgba(255,255,255,0.1); color: #fff;">Ctrl + Enter</kbd> to compile and optimize instantly.
      </div>
    </div>

    <!-- Right Column: Inspection & Optimization Tabs -->
    <div class="card">
      <div style="padding: 16px 18px 0 18px;">
        <div class="metrics-banner">
          <div class="metric-pill">
            <span class="metric-label">Initial TAC</span>
            <span id="metricInitial" class="metric-value">-</span>
          </div>
          <div class="metric-pill">
            <span class="metric-label">Optimized TAC</span>
            <span id="metricFinal" class="metric-value">-</span>
          </div>
          <div class="metric-pill">
            <span class="metric-label">Reduction</span>
            <span id="metricReduction" class="metric-value highlight">-</span>
          </div>
          <div class="metric-pill">
            <span class="metric-label">Iterations</span>
            <span id="metricIterations" class="metric-value">-</span>
          </div>
        </div>
      </div>

      <div class="tabs">
        <button class="tab-btn active" data-tab="tab-tac">TAC Comparison</button>
        <button class="tab-btn" data-tab="tab-stepper">Pass Stepper</button>
        <button class="tab-btn" data-tab="tab-ast">AST Viewer</button>
        <button class="tab-btn" data-tab="tab-symbols">Symbol Table</button>
        <button class="tab-btn" data-tab="tab-tokens">Token Stream</button>
        <button class="tab-btn" data-tab="tab-output">Output & Verifier</button>
      </div>

      <!-- Tab 1: TAC Comparison -->
      <div id="tab-tac" class="tab-content active">
        <div class="diff-grid">
          <div>
            <div class="diff-col-title">
              <span>Unoptimized TAC</span>
              <span id="unoptBadge" style="color: var(--warning);">0 instrs</span>
            </div>
            <pre id="unoptimizedTac" class="code-block">// Compile to view unoptimized TAC</pre>
          </div>
          <div>
            <div class="diff-col-title">
              <span>Optimized TAC</span>
              <span id="optBadge" style="color: var(--success);">0 instrs</span>
            </div>
            <pre id="optimizedTac" class="code-block">// Compile to view optimized TAC</pre>
          </div>
        </div>
      </div>

      <!-- Tab 2: Pass Stepper -->
      <div id="tab-stepper" class="tab-content">
        <div class="stepper-controls">
          <button id="prevStepBtn">&larr; Previous Pass</button>
          <span id="stepperInfo" class="stepper-info">Step 0 of 0</span>
          <button id="nextStepBtn">Next Pass &rarr;</button>
        </div>
        <div id="stepDetails" style="margin-bottom: 10px;">
          <h4 id="stepPassName" style="color: #fff; font-size: 0.95rem; margin-bottom: 4px;">Pass Name</h4>
          <ul id="stepLogsList" class="logs-list"></ul>
        </div>
        <pre id="stepTacBlock" class="code-block">// Step TAC will appear here</pre>
      </div>

      <!-- Tab 3: AST Viewer -->
      <div id="tab-ast" class="tab-content">
        <pre id="astBlock" class="code-block">// Parse to view Abstract Syntax Tree</pre>
      </div>

      <!-- Tab 4: Symbol Table -->
      <div id="tab-symbols" class="tab-content">
        <table class="data-table">
          <thead>
            <tr><th>Identifier</th><th>Declared At</th><th>Initialized</th></tr>
          </thead>
          <tbody id="symbolsTbody">
            <tr><td colspan="3" style="text-align: center; color: #64748b;">No symbols</td></tr>
          </tbody>
        </table>
      </div>

      <!-- Tab 5: Tokens -->
      <div id="tab-tokens" class="tab-content">
        <table class="data-table">
          <thead>
            <tr><th>Type</th><th>Lexeme</th><th>Literal</th><th>Location</th></tr>
          </thead>
          <tbody id="tokensTbody">
            <tr><td colspan="4" style="text-align: center; color: #64748b;">No tokens</td></tr>
          </tbody>
        </table>
      </div>

      <!-- Tab 6: Output & Verifier -->
      <div id="tab-output" class="tab-content">
        <div style="margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center;">
          <h4 style="font-size: 0.9rem; color: #94a3b8;">Semantic Equivalence Verification</h4>
          <span id="equivBadge" class="status-badge badge-success">READY</span>
        </div>
        <p id="equivMessage" style="font-size: 0.82rem; color: #cbd5e1; margin-bottom: 14px;">Verification will execute on compile.</p>
        <h4 style="font-size: 0.9rem; color: #94a3b8;">Standard Program Output (Console)</h4>
        <div id="outputBlock" class="output-box">> (no output)</div>
      </div>
    </div>
  </main>

  <script>
    let compilationSteps = [];
    let currentStepIndex = 0;

    // Tab switching
    document.querySelectorAll('.tab-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
        btn.classList.add('active');
        document.getElementById(btn.dataset.tab).classList.add('active');
      });
    });

    // Character counter
    const editor = document.getElementById('codeEditor');
    editor.addEventListener('input', () => {
      document.getElementById('charCount').textContent = editor.value.length + ' chars';
    });
    document.getElementById('charCount').textContent = editor.value.length + ' chars';

    // Hotkey: Ctrl+Enter to compile
    window.addEventListener('keydown', (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
        compileCode();
      }
    });

    // Load Example Programs from API
    async function loadExamples() {
      try {
        const res = await fetch('/api/examples');
        const examples = await res.json();
        const sel = document.getElementById('exampleSelector');
        examples.forEach(ex => {
          const opt = document.createElement('option');
          opt.value = ex.code;
          opt.textContent = ex.name;
          sel.appendChild(opt);
        });
        sel.addEventListener('change', () => {
          if (sel.value) {
            editor.value = sel.value;
            document.getElementById('charCount').textContent = editor.value.length + ' chars';
            compileCode();
          }
        });
      } catch (err) {
        console.error('Failed to load examples:', err);
      }
    }

    async function compileCode() {
      const source = editor.value;
      const pass = document.getElementById('passSelector').value;
      const btn = document.getElementById('runBtn');
      btn.disabled = true;
      btn.style.opacity = '0.7';

      try {
        const res = await fetch('/api/compile', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ source: source, passFilter: pass })
        });
        const data = await res.json();

        if (!data.success) {
          alert('Error [' + data.errorType + ']: ' + data.message);
          return;
        }

        // Metrics
        document.getElementById('metricInitial').textContent = data.unoptimizedCount;
        document.getElementById('metricFinal').textContent = data.optimizedCount;
        document.getElementById('metricReduction').textContent = data.reductionPercentage.toFixed(1) + '%';
        document.getElementById('metricIterations').textContent = data.iterations;

        // TAC
        document.getElementById('unoptimizedTac').textContent = data.unoptimizedTac;
        document.getElementById('optimizedTac').textContent = data.optimizedTac;
        document.getElementById('unoptBadge').textContent = data.unoptimizedCount + ' instrs';
        document.getElementById('optBadge').textContent = data.optimizedCount + ' instrs';

        // AST
        document.getElementById('astBlock').textContent = data.ast;

        // Symbols
        const symTbody = document.getElementById('symbolsTbody');
        symTbody.innerHTML = '';
        if (data.symbols && data.symbols.length > 0) {
          data.symbols.forEach(s => {
            const tr = document.createElement('tr');
            tr.innerHTML = '<td>' + s.name + '</td><td>' + s.location + '</td><td><span style="color:' + (s.initialized ? '#34d399' : '#f87171') + '">' + s.initialized + '</span></td>';
            symTbody.appendChild(tr);
          });
        } else {
          symTbody.innerHTML = '<tr><td colspan="3" style="text-align: center; color: #64748b;">No variables declared</td></tr>';
        }

        // Tokens
        const tokTbody = document.getElementById('tokensTbody');
        tokTbody.innerHTML = '';
        if (data.tokens) {
          data.tokens.forEach(t => {
            const tr = document.createElement('tr');
            tr.innerHTML = '<td>' + t.type + '</td><td><code>' + t.lexeme + '</code></td><td>' + (t.literal !== null ? t.literal : '-') + '</td><td>' + t.location + '</td>';
            tokTbody.appendChild(tr);
          });
        }

        // Stepper
        compilationSteps = data.steps || [];
        currentStepIndex = 0;
        updateStepperView();

        // Output & Verifier
        const equivBadge = document.getElementById('equivBadge');
        if (data.equivalent) {
          equivBadge.className = 'status-badge badge-success';
          equivBadge.textContent = 'PASSED';
        } else {
          equivBadge.className = 'status-badge badge-danger';
          equivBadge.textContent = 'FAILED';
        }
        document.getElementById('equivMessage').textContent = data.verificationMessage;

        const outBlock = document.getElementById('outputBlock');
        if (data.output && data.output.length > 0) {
          outBlock.innerHTML = data.output.map(line => '> ' + line).join('<br>');
        } else {
          outBlock.innerHTML = '> (no print statements executed)';
        }

      } catch (err) {
        alert('Network/Server error: ' + err.message);
      } finally {
        btn.disabled = false;
        btn.style.opacity = '1';
      }
    }

    function updateStepperView() {
      if (compilationSteps.length === 0) {
        document.getElementById('stepperInfo').textContent = 'No pass changes';
        document.getElementById('stepPassName').textContent = 'Fixpoint reached without transformations';
        document.getElementById('stepLogsList').innerHTML = '<li>Program already optimal or no transformations applied.</li>';
        document.getElementById('stepTacBlock').textContent = document.getElementById('optimizedTac').textContent;
        return;
      }

      const step = compilationSteps[currentStepIndex];
      document.getElementById('stepperInfo').textContent = 'Step ' + (currentStepIndex + 1) + ' of ' + compilationSteps.length;
      document.getElementById('stepPassName').textContent = 'Iteration ' + step.iteration + ': ' + step.passName + ' (' + step.changes + ' change(s))';

      const logsList = document.getElementById('stepLogsList');
      logsList.innerHTML = '';
      step.logs.forEach(log => {
        const li = document.createElement('li');
        li.textContent = log;
        logsList.appendChild(li);
      });

      document.getElementById('stepTacBlock').textContent = step.tac;
    }

    document.getElementById('prevStepBtn').addEventListener('click', () => {
      if (currentStepIndex > 0) {
        currentStepIndex--;
        updateStepperView();
      }
    });

    document.getElementById('nextStepBtn').addEventListener('click', () => {
      if (currentStepIndex < compilationSteps.length - 1) {
        currentStepIndex++;
        updateStepperView();
      }
    });

    document.getElementById('runBtn').addEventListener('click', compileCode);

    // Initial load
    loadExamples();
    compileCode();
  </script>
</body>
</html>
""";
}
