package com.hospital.seed;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

import com.hospital.entity.Bed;
import com.hospital.entity.Department;
import com.hospital.entity.DocumentCategory;
import com.hospital.entity.Gender;
import com.hospital.entity.KnowledgeDocument;
import com.hospital.entity.Patient;
import com.hospital.entity.Visit;
import com.hospital.entity.VisitStatus;
import com.hospital.repository.BedRepository;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DocumentCategoryRepository;
import com.hospital.repository.KnowledgeDocumentRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.VisitRepository;

/**
 * Populates the database (and, for the Knowledge Center, the local
 * filesystem) with demo data on first startup so the application is
 * immediately usable. Runs only when the department table is empty, and is
 * disabled entirely in the "test" profile (see application.properties).
 */
@ApplicationScoped
public class DataSeeder {

    private static final Logger LOG = Logger.getLogger(DataSeeder.class);
    private static final Random RANDOM = new Random(42);

    /** name, code, description, bed count, target occupied beds, waiting minutes [min,max], charge [min,max] */
    private record DepartmentSeed(String name, String code, String description, int bedCount, int occupiedBeds,
            int waitMin, int waitMax, int chargeMin, int chargeMax) {
    }

    private static final List<DepartmentSeed> DEPARTMENTS = List.of(
            new DepartmentSeed("Emergency", "ER", "24/7 emergency and trauma care", 20, 14, 20, 90, 150, 900),
            new DepartmentSeed("ICU", "ICU", "Intensive care for critically ill patients", 15, 14, 5, 20, 2500, 9000),
            new DepartmentSeed("Cardiology", "CARD", "Heart and cardiovascular care", 25, 14, 10, 40, 300, 2500),
            new DepartmentSeed("Surgery", "SURG", "Surgical procedures and post-operative recovery", 12, 7, 15, 45, 3000, 15000),
            new DepartmentSeed("Outpatient", "OPD", "Scheduled outpatient visits and consultations", 0, 0, 5, 30, 80, 400));

    private static final List<String> FIRST_NAMES = List.of(
            "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda", "David", "Elizabeth",
            "William", "Barbara", "Richard", "Susan", "Joseph", "Jessica", "Thomas", "Sarah", "Charles", "Karen",
            "Christopher", "Nancy", "Daniel", "Lisa", "Matthew", "Betty", "Anthony", "Margaret", "Mark", "Sandra");

    private static final List<String> LAST_NAMES = List.of(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
            "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin");

    private static final Map<String, List<String>> VISIT_REASONS_BY_CODE = Map.of(
            "ER", List.of("Chest pain", "Severe injury", "Difficulty breathing", "High fever", "Laceration"),
            "ICU", List.of("Post-surgical critical care", "Respiratory failure", "Sepsis monitoring", "Cardiac arrest recovery"),
            "CARD", List.of("Chest pain evaluation", "Arrhythmia follow-up", "Hypertension management", "Post-cardiac checkup"),
            "SURG", List.of("Scheduled surgery", "Post-operative review", "Pre-surgical assessment", "Surgical consult"),
            "OPD", List.of("Annual checkup", "Follow-up consultation", "Routine vaccination", "General consultation"));

    private static final List<String> CATEGORY_NAMES = List.of("Policy", "SOP", "Clinical Guideline", "Report");

    private record DocumentSeed(String title, String category) {
    }

    private static final List<DocumentSeed> DOCUMENTS = List.of(
            new DocumentSeed("ICU Escalation Protocol", "Policy"),
            new DocumentSeed("Emergency Department Triage SOP", "SOP"),
            new DocumentSeed("Infection Control Guideline", "Clinical Guideline"),
            new DocumentSeed("Surgical Safety Checklist", "Clinical Guideline"),
            new DocumentSeed("Patient Discharge SOP", "SOP"),
            new DocumentSeed("Q1 2026 Hospital Quality Report", "Report"));

    private static final int GUARANTEED_TODAY_VISITS = 8;

    @ConfigProperty(name = "hospital.seed.enabled", defaultValue = "true")
    boolean seedEnabled;

    @ConfigProperty(name = "hospital.knowledge.storage-path")
    String knowledgeStoragePath;

    private final DepartmentRepository departmentRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final BedRepository bedRepository;
    private final DocumentCategoryRepository documentCategoryRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    public DataSeeder(DepartmentRepository departmentRepository, PatientRepository patientRepository,
            VisitRepository visitRepository, BedRepository bedRepository,
            DocumentCategoryRepository documentCategoryRepository,
            KnowledgeDocumentRepository knowledgeDocumentRepository) {
        this.departmentRepository = departmentRepository;
        this.patientRepository = patientRepository;
        this.visitRepository = visitRepository;
        this.bedRepository = bedRepository;
        this.documentCategoryRepository = documentCategoryRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    }

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (!seedEnabled) {
            return;
        }
        if (departmentRepository.count() > 0) {
            LOG.info("Seed data already present, skipping seeding.");
            return;
        }

        List<Department> departments = seedDepartments();
        int bedCount = seedBeds(departments);
        List<Patient> patients = seedPatients(50);
        seedVisits(200, departments, patients);
        int documentCount = seedKnowledgeCenter();

        LOG.infof("Seeded %d departments, %d beds, %d patients, %d visits, %d knowledge documents.",
                departments.size(), bedCount, patients.size(), 200, documentCount);
    }

    private List<Department> seedDepartments() {
        return DEPARTMENTS.stream()
                .map(seed -> {
                    Department department = new Department();
                    department.name = seed.name();
                    department.code = seed.code();
                    department.description = seed.description();
                    departmentRepository.persist(department);
                    return department;
                })
                .toList();
    }

    private int seedBeds(List<Department> departments) {
        int total = 0;
        for (Department department : departments) {
            DepartmentSeed seed = seedFor(department.code);
            for (int i = 1; i <= seed.bedCount(); i++) {
                Bed bed = new Bed();
                bed.department = department;
                bed.code = "%s-%02d".formatted(seed.code(), i);
                bed.occupied = i <= seed.occupiedBeds();
                bedRepository.persist(bed);
                total++;
            }
        }
        return total;
    }

    private List<Patient> seedPatients(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> {
                    String firstName = pick(FIRST_NAMES);
                    String lastName = pick(LAST_NAMES);
                    Patient patient = new Patient();
                    patient.firstName = firstName;
                    patient.lastName = lastName;
                    patient.dateOfBirth = randomBirthDate();
                    patient.gender = Gender.values()[RANDOM.nextInt(Gender.values().length)];
                    patient.phone = randomPhone();
                    patient.email = (firstName + "." + lastName + i + "@example.test").toLowerCase();
                    patient.address = (100 + RANDOM.nextInt(900)) + " Main Street";
                    patientRepository.persist(patient);
                    return patient;
                })
                .toList();
    }

    private void seedVisits(int count, List<Department> departments, List<Patient> patients) {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < count; i++) {
            Department department = i < GUARANTEED_TODAY_VISITS
                    // Rotate through every department for the first few "today" visits so the
                    // Executive Dashboard's per-department cards (Surgeries, Emergency Cases)
                    // are never zero on a fresh checkout.
                    ? departments.get(i % departments.size())
                    : pick(departments);
            DepartmentSeed seed = seedFor(department.code);

            Visit visit = new Visit();
            visit.patient = pick(patients);
            visit.department = department;
            visit.visitDate = i < GUARANTEED_TODAY_VISITS
                    ? now.withHour(8 + i).withMinute(0).withSecond(0).withNano(0)
                    // Uniformly spread across the past 30 days and the next 6 days.
                    : now.plusDays(RANDOM.nextInt(37) - 30)
                            .withHour(8 + RANDOM.nextInt(9)).withMinute(RANDOM.nextInt(60)).withSecond(0).withNano(0);
            visit.reason = pick(VISIT_REASONS_BY_CODE.get(department.code));
            visit.status = visit.visitDate.isAfter(now) ? VisitStatus.SCHEDULED
                    : VisitStatus.values()[1 + RANDOM.nextInt(2)];
            visit.notes = null;
            visit.waitingMinutes = seed.waitMin() + RANDOM.nextInt(seed.waitMax() - seed.waitMin() + 1);
            visit.charge = BigDecimal.valueOf(seed.chargeMin() + RANDOM.nextInt(seed.chargeMax() - seed.chargeMin() + 1));
            visitRepository.persist(visit);
        }
    }

    private int seedKnowledgeCenter() {
        Map<String, DocumentCategory> categories = CATEGORY_NAMES.stream()
                .collect(java.util.stream.Collectors.toMap(name -> name, name -> {
                    DocumentCategory category = new DocumentCategory();
                    category.name = name;
                    documentCategoryRepository.persist(category);
                    return category;
                }));

        Path storageRoot = Path.of(knowledgeStoragePath);
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create Knowledge Center storage directory", e);
        }

        int count = 0;
        for (DocumentSeed docSeed : DOCUMENTS) {
            byte[] pdfBytes = PdfGenerator.generate(docSeed.title());
            String storedFileName = UUID.randomUUID() + ".pdf";
            try {
                Files.write(storageRoot.resolve(storedFileName), pdfBytes);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write seeded knowledge document", e);
            }

            KnowledgeDocument document = new KnowledgeDocument();
            document.title = docSeed.title();
            document.category = categories.get(docSeed.category());
            document.fileName = docSeed.title().replace(' ', '_') + ".pdf";
            document.contentType = "application/pdf";
            document.fileSize = pdfBytes.length;
            document.storagePath = storedFileName;
            knowledgeDocumentRepository.persist(document);
            count++;
        }
        return count;
    }

    private DepartmentSeed seedFor(String code) {
        return DEPARTMENTS.stream().filter(d -> d.code().equals(code)).findFirst().orElseThrow();
    }

    private LocalDate randomBirthDate() {
        int year = 1945 + RANDOM.nextInt(70);
        int month = 1 + RANDOM.nextInt(12);
        int day = 1 + RANDOM.nextInt(28);
        return LocalDate.of(year, month, day);
    }

    private String randomPhone() {
        return String.format("555-%03d-%04d", RANDOM.nextInt(1000), RANDOM.nextInt(10000));
    }

    private <T> T pick(List<T> items) {
        return items.get(RANDOM.nextInt(items.size()));
    }
}
