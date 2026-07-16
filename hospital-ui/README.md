# hospital-ui

React + TypeScript + Vite + Tailwind CSS admin UI. Sprint 1: Departments,
Patients, Visits CRUD. Sprint 2: Executive Dashboard, Command Center,
AI Director chat, and Knowledge Center. Sprint D1: Hospital Discovery — a
Notion/Jira-style survey workspace, independent of the Mini HIS pages. See
the root [README.md](../README.md) for full run instructions, requirements,
feature descriptions, and demo scripts.

## Quick start

```bash
npm install
npm run dev
```

Requires `hospital-backend` running at `http://localhost:8080` (override
with a `VITE_API_BASE_URL` env var if needed).

## Tests

```bash
npm test
```

Component tests via Vitest + React Testing Library.

## Layout

```
src/
  api/          # Typed fetch client + one module per resource (departments, patients, visits,
                #   dashboard, commandCenter, aiDirector, knowledge, discoveryProjects,
                #   discoverySurvey, discoveryAttachments, discoveryDashboard)
  components/
    layout/     # Sidebar (grouped: Copilot / Records / Hospital Discovery), TopNav, page Layout
    common/     # Reusable UI: LoadingState, ErrorState, ConfirmDialog, Pagination, Modal,
                #   StatCard, SeverityBadge, ChatMessage, ProgressBar, etc.
    forms/      # Per-entity create/edit/upload forms with client-side validation
    discovery/  # DiscoveryProjectForm, QuestionCard, AnswerInput (per-answer-type), AttachmentPanel
  hooks/        # useResourceList (pagination/search/sort), useAutoSaveAnswer (debounced answer save)
  lib/          # Formatting helpers (currency, percent, minutes)
  pages/        # DashboardPage, CommandCenterPage, AiDirectorPage, KnowledgeCenterPage,
                #   DepartmentsPage, PatientsPage, VisitsPage
    discovery/  # DiscoveryDashboardPage, DiscoveryProjectsPage, DiscoveryProjectWorkspacePage
```

## AI Director UI

`AiDirectorPage` is a plain chat UI (message list + input + example-question
chips) talking to `POST /api/ai-director/ask`. It renders whatever
`{ answer, intent, data }` the backend returns and has no knowledge of how
the answer was produced — swapping the backend's rule engine for a real LLM
later requires no changes here.

## Hospital Discovery workspace UI

`DiscoveryProjectWorkspacePage` renders a sidebar of sections (each with an
answered/total badge, fetched once) and a main panel of `QuestionCard`s for
whichever section is selected. Each `QuestionCard` owns its own auto-save
via `useAutoSaveAnswer` — there is deliberately no single form/submit button
for the whole survey; every question saves independently ~600ms after you
stop editing it, so a slow save on one question never blocks the others.
`AnswerInput` switches rendering by `answerType` (text/number/date/yes-no/
single-choice/multiple-choice/rating/file-attachment/url) but the question
catalog itself is never hardcoded here — it's whatever the API returns.
