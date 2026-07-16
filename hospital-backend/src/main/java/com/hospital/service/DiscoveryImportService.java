package com.hospital.service;

import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import com.hospital.dto.DiscoveryProjectResponse;
import com.hospital.dto.DiscoverySurveyExport;
import com.hospital.entity.DiscoveryAnswer;
import com.hospital.entity.DiscoveryProject;
import com.hospital.entity.DiscoveryProjectStatus;
import com.hospital.entity.DiscoveryQuestion;
import com.hospital.exception.InvalidFileException;
import com.hospital.mapper.DiscoveryProjectMapper;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryProjectRepository;
import com.hospital.repository.DiscoveryQuestionRepository;

/**
 * Imports a {@link DiscoverySurveyExport} JSON document as a brand-new
 * project (never overwrites an existing one). Answers are matched back to
 * the current question catalog by {@code code}; a question code that no
 * longer exists in this environment's catalog is skipped rather than
 * failing the whole import, since the catalog can evolve independently of
 * any one export.
 *
 * <p>The question catalog is loaded once and matched in memory — with
 * ~200 questions in an export, one lookup query per question turned import
 * into a many-second operation against a remote database.
 */
@ApplicationScoped
public class DiscoveryImportService {

    private static final Logger LOG = Logger.getLogger(DiscoveryImportService.class);

    private final DiscoveryProjectRepository discoveryProjectRepository;
    private final DiscoveryQuestionRepository discoveryQuestionRepository;
    private final DiscoveryAnswerRepository discoveryAnswerRepository;
    private final DiscoveryProjectMapper discoveryProjectMapper;
    private final DiscoveryProgressCalculator progressCalculator;

    public DiscoveryImportService(DiscoveryProjectRepository discoveryProjectRepository,
            DiscoveryQuestionRepository discoveryQuestionRepository,
            DiscoveryAnswerRepository discoveryAnswerRepository, DiscoveryProjectMapper discoveryProjectMapper,
            DiscoveryProgressCalculator progressCalculator) {
        this.discoveryProjectRepository = discoveryProjectRepository;
        this.discoveryQuestionRepository = discoveryQuestionRepository;
        this.discoveryAnswerRepository = discoveryAnswerRepository;
        this.discoveryProjectMapper = discoveryProjectMapper;
        this.progressCalculator = progressCalculator;
    }

    @Transactional
    public DiscoveryProjectResponse importSurvey(DiscoverySurveyExport export) {
        if (export == null || export.project() == null || export.sections() == null) {
            throw new InvalidFileException("The uploaded file is not a valid survey export");
        }

        DiscoveryProject project = new DiscoveryProject();
        project.projectName = export.project().projectName();
        project.hospitalName = export.project().hospitalName();
        project.contactPerson = export.project().contactPerson();
        project.contactEmail = export.project().contactEmail();
        project.contactPhone = export.project().contactPhone();
        project.surveyDate = export.project().surveyDate();
        project.status = export.project().status() != null ? export.project().status() : DiscoveryProjectStatus.DRAFT;
        project.notes = export.project().notes();
        discoveryProjectRepository.persist(project);

        Map<String, DiscoveryQuestion> questionsByCode = discoveryQuestionRepository.listAll().stream()
                .collect(Collectors.toMap(q -> q.code, q -> q));

        int imported = 0;
        int skipped = 0;
        for (var section : export.sections()) {
            for (var questionExport : section.questions()) {
                if (questionExport.answer() == null) {
                    continue;
                }
                DiscoveryQuestion question = questionsByCode.get(questionExport.code());
                if (question == null) {
                    skipped++;
                    continue;
                }
                DiscoveryAnswer answer = new DiscoveryAnswer();
                answer.project = project;
                answer.question = question;
                answer.answerValue = questionExport.answer().value();
                answer.comment = questionExport.answer().comment();
                answer.riskLevel = questionExport.answer().riskLevel();
                discoveryAnswerRepository.persist(answer);
                imported++;
            }
        }

        LOG.infof("Imported discovery project '%s': %d answers imported, %d skipped (question code not found).",
                project.projectName, imported, skipped);

        return discoveryProjectMapper.toResponse(project, progressCalculator.percentOnly(project.id));
    }
}
