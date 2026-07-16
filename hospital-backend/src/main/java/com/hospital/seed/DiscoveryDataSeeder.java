package com.hospital.seed;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

import com.hospital.entity.DiscoveryAnswer;
import com.hospital.entity.DiscoveryAnswerType;
import com.hospital.entity.DiscoveryProject;
import com.hospital.entity.DiscoveryProjectStatus;
import com.hospital.entity.DiscoveryQuestion;
import com.hospital.entity.DiscoveryRiskLevel;
import com.hospital.entity.DiscoverySection;
import com.hospital.mapper.DiscoveryQuestionMapper;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryProjectRepository;
import com.hospital.repository.DiscoverySectionRepository;
import com.hospital.repository.DiscoveryQuestionRepository;

/**
 * Populates the Hospital Discovery questionnaire catalog (sections +
 * questions) on first startup, plus two sample survey projects with partial
 * answers so the Dashboard and Projects list are never empty on first run.
 * Entirely independent of {@link DataSeeder} (Mini HIS).
 */
@ApplicationScoped
public class DiscoveryDataSeeder {

    private static final Logger LOG = Logger.getLogger(DiscoveryDataSeeder.class);

    private record QuestionSeed(String codeSuffix, String title, String description, DiscoveryAnswerType answerType,
            List<String> options) {

        private QuestionSeed(String codeSuffix, String title, DiscoveryAnswerType answerType) {
            this(codeSuffix, title, null, answerType, null);
        }

        private QuestionSeed(String codeSuffix, String title, DiscoveryAnswerType answerType, List<String> options) {
            this(codeSuffix, title, null, answerType, options);
        }
    }

    private record SectionSeed(String code, String name, String description, List<QuestionSeed> questions) {
    }

    private static final List<SectionSeed> SECTIONS = List.of(
            new SectionSeed("OVERVIEW", "Hospital Overview", "General facts about the hospital being surveyed.",
                    List.of(
                            new QuestionSeed("01", "What is the official name and address of the hospital?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("02", "What type of hospital is this?", DiscoveryAnswerType.SINGLE_CHOICE,
                                    List.of("General", "Specialty", "Teaching", "Rehabilitation", "Other")),
                            new QuestionSeed("03", "How many licensed beds does the hospital operate?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("04", "How many outpatient visits does the hospital handle per month on average?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("05", "How many inpatient admissions does the hospital handle per month on average?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("06", "What is the hospital's ownership type?", DiscoveryAnswerType.SINGLE_CHOICE,
                                    List.of("Public", "Private", "Non-profit", "Public-Private Partnership")),
                            new QuestionSeed("07", "How many staff (clinical and non-clinical) does the hospital employ?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("08", "What are the hospital's primary clinical specialties?", DiscoveryAnswerType.MULTIPLE_CHOICE,
                                    List.of("Emergency", "Cardiology", "Surgery", "Pediatrics", "Oncology", "Obstetrics", "Orthopedics", "ICU", "Other")),
                            new QuestionSeed("09", "Does the hospital operate multiple campuses or branches?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("10", "What is the hospital's accreditation status (e.g. JCI)?", DiscoveryAnswerType.TEXT))),
            new SectionSeed("ORG", "Organization", "Organizational structure and governance.",
                    List.of(
                            new QuestionSeed("01", "Is there a documented organizational chart?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "Who is the executive sponsor for this digital transformation project?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "Does the hospital have a dedicated IT department?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("04", "How many staff work in the IT department?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("05", "Is there a Chief Information Officer (CIO) or equivalent role?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("06", "How are clinical departments structured (by specialty, building, etc.)?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("07", "Is there a Change Management / Project Management Office (PMO)?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("08", "Who owns data governance decisions today?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("09", "How mature is the hospital's decision-making process for IT investments?", DiscoveryAnswerType.RATING),
                            new QuestionSeed("10", "Are there designated department champions for new system rollouts?", DiscoveryAnswerType.YES_NO))),
            new SectionSeed("PROCESS", "Business Process", "How core hospital workflows operate today.",
                    List.of(
                            new QuestionSeed("01", "Is the patient registration process manual, digital, or hybrid?", DiscoveryAnswerType.SINGLE_CHOICE,
                                    List.of("Manual", "Digital", "Hybrid")),
                            new QuestionSeed("02", "How are appointments currently scheduled?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "Is there a documented patient admission-to-discharge workflow?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("04", "How long does patient registration take on average (minutes)?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("05", "Are clinical handover processes standardized across departments?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("06", "How are referrals between departments handled today?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("07", "Is there a formal process for tracking bed availability?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("08", "How are billing and payment processes currently handled?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("09", "Are Standard Operating Procedures (SOPs) documented and accessible to staff?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("10", "What is the average patient wait time in the Emergency Department (minutes)?", DiscoveryAnswerType.NUMBER))),
            new SectionSeed("ITLAND", "IT Landscape", "The current application and systems landscape.",
                    List.of(
                            new QuestionSeed("01", "List all major software systems currently in use.", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("02", "How many separate systems does clinical staff need to log into daily?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("03", "Are systems cloud-hosted, on-premise, or hybrid?", DiscoveryAnswerType.SINGLE_CHOICE,
                                    List.of("Cloud", "On-Premise", "Hybrid")),
                            new QuestionSeed("04", "Is there an existing systems architecture diagram?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("05", "Who is responsible for IT system maintenance (in-house or vendor)?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("06", "What is the average age of the hospital's core systems (years)?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("07", "Are there known integration gaps between systems today?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("08", "Is there a formal IT asset inventory?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("09", "What is the hospital's annual IT budget (approximate)?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("10", "Upload any existing system architecture documentation.", DiscoveryAnswerType.FILE_ATTACHMENT))),
            new SectionSeed("HIS", "HIS", "Hospital Information System.",
                    List.of(
                            new QuestionSeed("01", "Does the hospital currently use a Hospital Information System (HIS)?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "What is the name and vendor of the current HIS?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "How many years has the current HIS been in use?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("04", "What modules does the current HIS cover?", DiscoveryAnswerType.MULTIPLE_CHOICE,
                                    List.of("Registration", "Billing", "Scheduling", "Bed Management", "Inventory", "Reporting", "Other")),
                            new QuestionSeed("05", "Is the HIS web-based or client-installed?", DiscoveryAnswerType.SINGLE_CHOICE,
                                    List.of("Web-based", "Client-installed", "Both")),
                            new QuestionSeed("06", "How satisfied is staff with the current HIS?", DiscoveryAnswerType.RATING),
                            new QuestionSeed("07", "What are the biggest pain points with the current HIS?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("08", "Does the HIS support role-based access control?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("09", "Is the HIS vendor still providing active support?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("10", "Upload a sample HIS user manual or system overview if available.", DiscoveryAnswerType.FILE_ATTACHMENT))),
            new SectionSeed("EMR", "EMR", "Electronic Medical Record.",
                    List.of(
                            new QuestionSeed("01", "Does the hospital use an Electronic Medical Record (EMR) system?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "What is the name and vendor of the current EMR?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "What percentage of clinical documentation is currently digital?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("04", "Does the EMR support structured data entry (not just free text)?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("05", "Are clinical order sets (CPOE) implemented?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("06", "Does the EMR support e-prescribing?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("07", "Is patient history accessible across all departments in the EMR?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("08", "What EMR standard is used for interoperability?", DiscoveryAnswerType.SINGLE_CHOICE,
                                    List.of("HL7 v2", "HL7 FHIR", "Proprietary", "None", "Other")),
                            new QuestionSeed("09", "How mature is clinical adoption of the EMR?", DiscoveryAnswerType.RATING),
                            new QuestionSeed("10", "What are the top three EMR improvement priorities?", DiscoveryAnswerType.TEXT))),
            new SectionSeed("LIS", "LIS", "Laboratory Information System.",
                    List.of(
                            new QuestionSeed("01", "Does the hospital use a Laboratory Information System (LIS)?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "What is the name and vendor of the current LIS?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "Is the LIS integrated with laboratory analyzers?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("04", "Is the LIS integrated with the HIS/EMR?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("05", "How are lab results delivered to clinicians today?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("06", "What is the average lab result turnaround time (hours)?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("07", "Does the LIS support barcode specimen tracking?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("08", "How many lab tests are processed per day on average?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("09", "Is there a quality control/quality assurance module in the LIS?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("10", "What are the main LIS pain points today?", DiscoveryAnswerType.TEXT))),
            new SectionSeed("PACS", "PACS", "Picture Archiving and Communication System.",
                    List.of(
                            new QuestionSeed("01", "Does the hospital use a PACS for medical imaging?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "What is the name and vendor of the current PACS?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "What imaging modalities are connected to the PACS?", DiscoveryAnswerType.MULTIPLE_CHOICE,
                                    List.of("X-ray", "CT", "MRI", "Ultrasound", "Mammography", "Other")),
                            new QuestionSeed("04", "Is the PACS integrated with the RIS/HIS?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("05", "How is imaging data currently archived and backed up?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("06", "What is the total imaging storage volume (approximate, in TB)?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("07", "Does the PACS support remote/mobile image viewing?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("08", "Is DICOM the standard format used across all modalities?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("09", "How long are images retained before archival/deletion?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("10", "What are the biggest PACS-related challenges today?", DiscoveryAnswerType.TEXT))),
            new SectionSeed("RIS", "RIS", "Radiology Information System.",
                    List.of(
                            new QuestionSeed("01", "Does the hospital use a Radiology Information System (RIS)?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "What is the name and vendor of the current RIS?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "Is the RIS integrated with the PACS?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("04", "How are radiology orders currently placed?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("05", "What is the average radiology report turnaround time (hours)?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("06", "Does the RIS support structured reporting templates?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("07", "How many radiology exams are performed per day on average?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("08", "Is there a radiologist workload/scheduling module?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("09", "Are critical findings flagged and tracked in the RIS?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("10", "What are the main RIS improvement priorities?", DiscoveryAnswerType.TEXT))),
            new SectionSeed("ERP", "ERP", "Enterprise Resource Planning (finance, procurement, assets).",
                    List.of(
                            new QuestionSeed("01", "Does the hospital use an ERP system for finance/supply chain?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "What is the name and vendor of the current ERP?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "What ERP modules are in use?", DiscoveryAnswerType.MULTIPLE_CHOICE,
                                    List.of("Finance", "Procurement", "Inventory", "Assets", "Payroll", "Other")),
                            new QuestionSeed("04", "Is inventory management integrated with clinical departments?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("05", "How is procurement currently approved and tracked?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("06", "Does the ERP integrate with the hospital's accounting system?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("07", "Is asset/equipment maintenance tracked in the ERP?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("08", "How satisfied is finance staff with the current ERP?", DiscoveryAnswerType.RATING),
                            new QuestionSeed("09", "What are the top ERP pain points today?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("10", "Is there a plan to replace or upgrade the ERP?", DiscoveryAnswerType.YES_NO))),
            new SectionSeed("HRM", "HRM", "Human Resource Management.",
                    List.of(
                            new QuestionSeed("01", "Does the hospital use an HR Management system?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "What is the name and vendor of the current HRM system?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "Is staff scheduling/rostering managed digitally?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("04", "Does the HRM system track staff credentials and licenses?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("05", "Is payroll integrated with the HRM system?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("06", "How is staff time and attendance currently tracked?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("07", "Does the HRM system support performance evaluation workflows?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("08", "How many total employees are managed in the system?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("09", "What are the top HRM pain points today?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("10", "Is there a training/competency management module?", DiscoveryAnswerType.YES_NO))),
            new SectionSeed("DWH", "Data Warehouse", "Existing data consolidation and analytics capability.",
                    List.of(
                            new QuestionSeed("01", "Does the hospital have any existing data warehouse or reporting database?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "What technology is used, if any (e.g. SQL Server, Oracle)?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "Is data currently consolidated from multiple source systems?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("04", "How is historical data currently retained and accessed?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("05", "Are there defined ETL (Extract-Transform-Load) processes today?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("06", "Who is responsible for data quality today?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("07", "How far back does historical operational data go (years)?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("08", "Is there a data retention/archival policy?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("09", "What is the biggest barrier to hospital-wide analytics today?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("10", "Rate the overall data maturity of the organization.", DiscoveryAnswerType.RATING))),
            new SectionSeed("DASH", "Dashboard", "Current operational and executive reporting capability.",
                    List.of(
                            new QuestionSeed("01", "Does the hospital currently use any operational dashboards?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "What tool is used for dashboards today (Excel, Power BI, etc.)?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "Who are the primary consumers of dashboards today?", DiscoveryAnswerType.MULTIPLE_CHOICE,
                                    List.of("Executives", "Department Heads", "Clinicians", "Finance", "IT", "Other")),
                            new QuestionSeed("04", "How frequently are dashboards updated?", DiscoveryAnswerType.SINGLE_CHOICE,
                                    List.of("Real-time", "Hourly", "Daily", "Weekly", "Monthly")),
                            new QuestionSeed("05", "What are the top 3 metrics leadership wants visibility into?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("06", "Is there a single source of truth for hospital KPIs today?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("07", "How much manual effort goes into producing current reports (hours/week)?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("08", "Do department heads have access to their own operational data?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("09", "Rate the current satisfaction with reporting/dashboard capability.", DiscoveryAnswerType.RATING),
                            new QuestionSeed("10", "Upload a sample of an existing report or dashboard if available.", DiscoveryAnswerType.FILE_ATTACHMENT))),
            new SectionSeed("KPI", "KPI", "Key performance indicators currently tracked (or not).",
                    List.of(
                            new QuestionSeed("01", "What are the hospital's top strategic KPIs today?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("02", "Is bed occupancy rate tracked and reported regularly?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("03", "Is average length of stay (ALOS) tracked?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("04", "Is emergency department waiting time tracked?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("05", "Are readmission rates monitored?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("06", "Is patient satisfaction formally measured?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("07", "How are financial KPIs (revenue, cost per case) currently tracked?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("08", "Are clinical quality indicators benchmarked against national standards?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("09", "Who defines and owns KPI targets today?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("10", "How would you rate the organization's current KPI discipline?", DiscoveryAnswerType.RATING))),
            new SectionSeed("INTEG", "Integration", "Interoperability between existing systems.",
                    List.of(
                            new QuestionSeed("01", "Are the hospital's core systems integrated with each other today?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "What integration standards are currently used?", DiscoveryAnswerType.MULTIPLE_CHOICE,
                                    List.of("HL7 v2", "HL7 FHIR", "REST API", "Flat File", "None", "Other")),
                            new QuestionSeed("03", "Is there a middleware/integration engine in place?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("04", "What is the name of the integration engine/middleware, if any?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("05", "How many point-to-point integrations currently exist?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("06", "Are there known data duplication or sync issues between systems?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("07", "Is there an API gateway or similar integration layer?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("08", "How is integration currently monitored for failures?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("09", "Rate the overall integration maturity of the organization.", DiscoveryAnswerType.RATING),
                            new QuestionSeed("10", "Upload any existing integration architecture diagrams.", DiscoveryAnswerType.FILE_ATTACHMENT))),
            new SectionSeed("INFRA", "Infrastructure", "Network, servers, and physical IT infrastructure.",
                    List.of(
                            new QuestionSeed("01", "Is the hospital's core infrastructure on-premise, cloud, or hybrid?", DiscoveryAnswerType.SINGLE_CHOICE,
                                    List.of("On-Premise", "Cloud", "Hybrid")),
                            new QuestionSeed("02", "Does the hospital have a dedicated data center or server room?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("03", "What is the current internet bandwidth available (Mbps)?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("04", "Is there a documented disaster recovery (DR) plan?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("05", "What is the recovery time objective (RTO) for critical systems?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("06", "Are systems backed up regularly?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("07", "How is network infrastructure (LAN/WiFi) coverage across the hospital?", DiscoveryAnswerType.RATING),
                            new QuestionSeed("08", "Is there redundant power supply (UPS/generator) for critical systems?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("09", "What operating systems/platforms are predominantly used?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("10", "Is virtualization (VMware, Hyper-V, etc.) used for servers?", DiscoveryAnswerType.YES_NO))),
            new SectionSeed("SEC", "Security", "Information security posture and compliance.",
                    List.of(
                            new QuestionSeed("01", "Is there a documented information security policy?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "Does the hospital comply with any specific data protection regulation?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "Is multi-factor authentication (MFA) used for system access?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("04", "Is patient data encrypted at rest and in transit?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("05", "Has a security risk assessment been conducted in the last 12 months?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("06", "Is there a dedicated security officer or team?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("07", "How are user access rights reviewed and revoked?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("08", "Has the hospital experienced any data breach or security incident in the past 3 years?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("09", "Is there an incident response plan in place?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("10", "Rate the organization's overall security maturity.", DiscoveryAnswerType.RATING))),
            new SectionSeed("AIREADY", "AI Readiness", "Organizational and data readiness for AI adoption.",
                    List.of(
                            new QuestionSeed("01", "Has the hospital used any AI or machine learning tools before?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("02", "What is leadership's overall appetite for AI adoption?", DiscoveryAnswerType.RATING),
                            new QuestionSeed("03", "Is there clean, structured historical data available for AI training?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("04", "Are staff generally open to AI-assisted decision support?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("05", "What specific AI use cases is the hospital most interested in?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("06", "Is there budget allocated for AI initiatives?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("07", "Who would own AI governance and oversight?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("08", "Are there regulatory constraints on using AI in this hospital's jurisdiction?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("09", "What is the biggest perceived risk of adopting AI here?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("10", "Rate the hospital's overall digital/data maturity as a foundation for AI.", DiscoveryAnswerType.RATING))),
            new SectionSeed("RISK", "Risks", "Implementation risks identified during discovery.",
                    List.of(
                            new QuestionSeed("01", "What are the top 3 risks to a successful implementation?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("02", "Is there executive-level commitment to this project?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("03", "Is staff turnover a significant concern for project continuity?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("04", "Are there known budget constraints that could affect the project?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("05", "Is resistance to change anticipated from clinical staff?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("06", "Are there any pending regulatory or compliance deadlines to consider?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("07", "Is the current infrastructure a risk to a new system rollout?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("08", "Rate the overall project risk level based on this discovery.", DiscoveryAnswerType.RATING),
                            new QuestionSeed("09", "Are there competing IT projects that could affect resourcing?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("10", "What contingency plans exist if the primary vendor/timeline slips?", DiscoveryAnswerType.TEXT))),
            new SectionSeed("RECOMM", "Recommendations", "Solution architect recommendations following discovery.",
                    List.of(
                            new QuestionSeed("01", "What is the recommended implementation approach?", DiscoveryAnswerType.SINGLE_CHOICE,
                                    List.of("Phased", "Big-bang", "Pilot then rollout")),
                            new QuestionSeed("02", "What should be the first module/phase to implement?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("03", "What is the estimated project timeline (months)?", DiscoveryAnswerType.NUMBER),
                            new QuestionSeed("04", "What is the estimated budget range for this project?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("05", "Are there quick wins that can be delivered early?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("06", "What organizational changes are recommended to support this project?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("07", "What training programs should be planned for staff?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("08", "Should any existing systems be retired as part of this project?", DiscoveryAnswerType.YES_NO),
                            new QuestionSeed("09", "What are the recommended success metrics for this project?", DiscoveryAnswerType.TEXT),
                            new QuestionSeed("10", "Upload the final discovery summary report.", DiscoveryAnswerType.FILE_ATTACHMENT))));

    private static final Random RANDOM = new Random(7);

    @ConfigProperty(name = "hospital.seed.enabled", defaultValue = "true")
    boolean seedEnabled;

    private final DiscoverySectionRepository discoverySectionRepository;
    private final DiscoveryQuestionRepository discoveryQuestionRepository;
    private final DiscoveryQuestionMapper discoveryQuestionMapper;
    private final DiscoveryProjectRepository discoveryProjectRepository;
    private final DiscoveryAnswerRepository discoveryAnswerRepository;

    public DiscoveryDataSeeder(DiscoverySectionRepository discoverySectionRepository,
            DiscoveryQuestionRepository discoveryQuestionRepository, DiscoveryQuestionMapper discoveryQuestionMapper,
            DiscoveryProjectRepository discoveryProjectRepository, DiscoveryAnswerRepository discoveryAnswerRepository) {
        this.discoverySectionRepository = discoverySectionRepository;
        this.discoveryQuestionRepository = discoveryQuestionRepository;
        this.discoveryQuestionMapper = discoveryQuestionMapper;
        this.discoveryProjectRepository = discoveryProjectRepository;
        this.discoveryAnswerRepository = discoveryAnswerRepository;
    }

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (!seedEnabled) {
            return;
        }
        if (discoverySectionRepository.count() > 0) {
            LOG.info("Discovery questionnaire catalog already present, skipping seeding.");
            return;
        }

        List<DiscoveryQuestion> allQuestions = new java.util.ArrayList<>();
        int questionCount = 0;
        for (int i = 0; i < SECTIONS.size(); i++) {
            SectionSeed sectionSeed = SECTIONS.get(i);
            DiscoverySection section = new DiscoverySection();
            section.code = sectionSeed.code();
            section.name = sectionSeed.name();
            section.description = sectionSeed.description();
            section.displayOrder = i + 1;
            discoverySectionRepository.persist(section);

            for (int j = 0; j < sectionSeed.questions().size(); j++) {
                QuestionSeed questionSeed = sectionSeed.questions().get(j);
                DiscoveryQuestion question = new DiscoveryQuestion();
                question.section = section;
                question.code = sectionSeed.code() + "-" + questionSeed.codeSuffix();
                question.title = questionSeed.title();
                question.description = questionSeed.description();
                question.answerType = questionSeed.answerType();
                question.optionsJson = discoveryQuestionMapper.writeOptions(questionSeed.options());
                question.displayOrder = j + 1;
                discoveryQuestionRepository.persist(question);
                allQuestions.add(question);
                questionCount++;
            }
        }

        LOG.infof("Seeded discovery questionnaire catalog: %d sections, %d questions.", SECTIONS.size(), questionCount);

        DiscoveryProject inProgress = seedProject("Hanoi Heart Hospital Digital Transformation", "Hanoi Heart Hospital",
                "Dr. Nguyen Van A", "nguyen.van.a@hanoihearthospital.example", "+84-24-555-0101",
                LocalDate.now().minusDays(10), DiscoveryProjectStatus.IN_PROGRESS,
                "Initial discovery survey following the executive demo. Focused on cardiology and ICU readiness.");
        answerFraction(inProgress, allQuestions, 0.4);

        DiscoveryProject completed = seedProject("Riverside General Hospital IT Assessment", "Riverside General Hospital",
                "Sarah Thompson", "sarah.thompson@riversidegeneral.example", "+1-555-0199",
                LocalDate.now().minusDays(45), DiscoveryProjectStatus.COMPLETED,
                "Completed discovery ahead of Q3 vendor selection.");
        answerFraction(completed, allQuestions, 1.0);

        LOG.info("Seeded 2 sample discovery projects with answers.");
    }

    private DiscoveryProject seedProject(String projectName, String hospitalName, String contactPerson,
            String contactEmail, String contactPhone, LocalDate surveyDate, DiscoveryProjectStatus status,
            String notes) {
        DiscoveryProject project = new DiscoveryProject();
        project.projectName = projectName;
        project.hospitalName = hospitalName;
        project.contactPerson = contactPerson;
        project.contactEmail = contactEmail;
        project.contactPhone = contactPhone;
        project.surveyDate = surveyDate;
        project.status = status;
        project.notes = notes;
        discoveryProjectRepository.persist(project);
        return project;
    }

    // Only 1 in 6 answers carries a risk flag, and HIGH is rare — a believable
    // distribution for a real hospital survey, not an alarming one.
    private static final DiscoveryRiskLevel[] SAMPLE_RISK_LEVELS = {
            DiscoveryRiskLevel.LOW, DiscoveryRiskLevel.LOW, DiscoveryRiskLevel.MEDIUM,
            DiscoveryRiskLevel.MEDIUM, DiscoveryRiskLevel.HIGH,
    };

    private void answerFraction(DiscoveryProject project, List<DiscoveryQuestion> questions, double fraction) {
        int toAnswer = (int) Math.round(questions.size() * fraction);
        for (int i = 0; i < toAnswer; i++) {
            DiscoveryQuestion question = questions.get(i);
            DiscoveryAnswer answer = new DiscoveryAnswer();
            answer.project = project;
            answer.question = question;
            answer.answerValue = sampleAnswerValue(question);
            answer.riskLevel = RANDOM.nextInt(6) == 0 ? SAMPLE_RISK_LEVELS[RANDOM.nextInt(SAMPLE_RISK_LEVELS.length)]
                    : null;
            discoveryAnswerRepository.persist(answer);
        }
    }

    private String sampleAnswerValue(DiscoveryQuestion question) {
        return switch (question.answerType) {
            case YES_NO -> RANDOM.nextBoolean() ? "Yes" : "No";
            case NUMBER -> String.valueOf(10 + RANDOM.nextInt(490));
            case RATING -> String.valueOf(1 + RANDOM.nextInt(5));
            case DATE -> LocalDate.now().minusDays(RANDOM.nextInt(365)).toString();
            case SINGLE_CHOICE, MULTIPLE_CHOICE -> firstOption(question.optionsJson);
            default -> "To be confirmed during on-site interview.";
        };
    }

    private String firstOption(String optionsJson) {
        List<String> options = discoveryQuestionMapper.parseOptions(optionsJson);
        return options.isEmpty() ? "Unspecified" : options.get(0);
    }
}
