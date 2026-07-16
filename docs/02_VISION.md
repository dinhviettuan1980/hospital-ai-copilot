# 02 — Vision

## Vision Statement

> Give a Hospital Director a single, trustworthy place to ask questions about
> their hospital — in natural language — and receive answers grounded in
> real operational data and real institutional knowledge, backed by an
> architecture that any hospital IT team could understand, adapt, and
> replace piece by piece.

## Why This Project Exists

Hospital directors do not need another dashboard with more charts, and they
do not need a chatbot that hallucinates numbers. They need an **AI Director**
that can:

- Answer "what is happening" questions from live operational data (via the
  data warehouse and Text-to-SQL).
- Answer "what does policy say" questions from institutional knowledge (via
  the Knowledge Base and RAG).
- Combine both into a single, decision-support-oriented conversation.

The source proposal envisions this as a layered architecture: a data plane,
a knowledge plane, and an AI orchestration plane sitting on top of a
(minimal) hospital operational system. This project exists to prove that
architecture works, end-to-end, in a form small enough for one developer to
build and any engineer to read in an afternoon.

## What Success Looks Like

A student, architect, or AI engineer can clone this repository and:

1. Understand the full system architecture from the docs alone, before
   reading any code.
2. Stand up a minimal hospital data source, watch data flow into a warehouse
   and dashboard.
3. Ask the AI Director a natural-language question and see it correctly
   choose between RAG, Text-to-SQL, or a combination of both.
4. Identify exactly which module they would replace to adapt this to a real
   hospital deployment, and understand why that replacement would not
   require touching the rest of the system.

## What This Project Is Not Trying to Be

This is a **reference implementation**, not a product. It is not trying to
compete with commercial HIS/EHR vendors, not trying to support real patient
data, and not trying to cover every hospital department. Its value is
architectural clarity, not operational completeness.

## Guiding Metaphor

Think of this repository as an **annotated blueprint you can run** — every
box in the architecture diagram corresponds to a real, runnable (in later
sprints), replaceable module, and every module exists because the proposal
named it.

## Long-Term Aspiration (Beyond This Repository's Scope)

The proposal describes future extension points (Iceberg, Trino, Debezium,
integration with a real HIS) that a production deployment would need. This
project intentionally stops short of them, but documents them as extension
points so the architecture is never a dead end. See
[08_HIGH_LEVEL_ARCHITECTURE.md](08_HIGH_LEVEL_ARCHITECTURE.md) §6.
