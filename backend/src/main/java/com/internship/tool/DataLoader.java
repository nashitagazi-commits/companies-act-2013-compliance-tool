package com.internship.tool;

import com.internship.tool.entity.ComplianceRecord;
import com.internship.tool.entity.User;
import com.internship.tool.repository.ComplianceRecordRepository;
import com.internship.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final ComplianceRecordRepository complianceRecordRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedComplianceRecords();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            log.info("Users already seeded — skipping");
            return;
        }
        log.info("Seeding users...");
        userRepository.saveAll(List.of(
            User.builder()
                .fullName("Admin User")
                .email("admin@company.com")
                .password(passwordEncoder.encode("admin123"))
                .role(User.Role.ADMIN)
                .isActive(true)
                .build(),
            User.builder()
                .fullName("Manager User")
                .email("manager@company.com")
                .password(passwordEncoder.encode("manager123"))
                .role(User.Role.MANAGER)
                .isActive(true)
                .build(),
            User.builder()
                .fullName("Regular User")
                .email("user@company.com")
                .password(passwordEncoder.encode("user123"))
                .role(User.Role.USER)
                .isActive(true)
                .build()
        ));
        log.info("✅ Seeded 3 users");
    }

    private void seedComplianceRecords() {
        if (complianceRecordRepository.count() > 0) {
            log.info("Compliance records already seeded — skipping");
            return;
        }
        log.info("Seeding compliance records...");
        complianceRecordRepository.saveAll(List.of(

            // 1
            ComplianceRecord.builder()
                .title("Annual Return Filing - MGT-7")
                .description("Filing of Annual Return with Registrar of Companies")
                .sectionNumber("Section 92")
                .complianceType("Annual Filing")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(15))
                .assignedTo("manager@company.com")
                .companyName("Tata Consultancy Services Ltd")
                .filingFrequency("Annual")
                .penaltyAmount(50000.0)
                .remarks("Due within 60 days of AGM")
                .isDeleted(false)
                .build(),

            // 2
            ComplianceRecord.builder()
                .title("Financial Statements Filing - AOC-4")
                .description("Filing of Financial Statements with ROC")
                .sectionNumber("Section 137")
                .complianceType("Annual Filing")
                .status(ComplianceRecord.ComplianceStatus.IN_PROGRESS)
                .priority(ComplianceRecord.Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(30))
                .assignedTo("manager@company.com")
                .companyName("Infosys Limited")
                .filingFrequency("Annual")
                .penaltyAmount(100000.0)
                .remarks("Audited financials required")
                .isDeleted(false)
                .build(),

            // 3
            ComplianceRecord.builder()
                .title("Board Meeting - Q1")
                .description("Mandatory Board Meeting for Q1 review")
                .sectionNumber("Section 173")
                .complianceType("Board Meeting")
                .status(ComplianceRecord.ComplianceStatus.COMPLETED)
                .priority(ComplianceRecord.Priority.MEDIUM)
                .dueDate(LocalDate.now().minusDays(10))
                .assignedTo("admin@company.com")
                .companyName("Wipro Limited")
                .filingFrequency("Quarterly")
                .penaltyAmount(25000.0)
                .remarks("Minutes recorded and filed")
                .isDeleted(false)
                .build(),

            // 4
            ComplianceRecord.builder()
                .title("Director KYC - DIR-3 KYC")
                .description("Annual KYC for all Directors")
                .sectionNumber("Rule 12A")
                .complianceType("Director Compliance")
                .status(ComplianceRecord.ComplianceStatus.OVERDUE)
                .priority(ComplianceRecord.Priority.CRITICAL)
                .dueDate(LocalDate.now().minusDays(5))
                .assignedTo("user@company.com")
                .companyName("HCL Technologies Ltd")
                .filingFrequency("Annual")
                .penaltyAmount(5000.0)
                .remarks("DIN deactivation risk")
                .isDeleted(false)
                .build(),

            // 5
            ComplianceRecord.builder()
                .title("Statutory Audit Completion")
                .description("Completion of Statutory Audit for FY 2025-26")
                .sectionNumber("Section 139")
                .complianceType("Audit")
                .status(ComplianceRecord.ComplianceStatus.IN_PROGRESS)
                .priority(ComplianceRecord.Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(45))
                .assignedTo("manager@company.com")
                .companyName("Tech Mahindra Limited")
                .filingFrequency("Annual")
                .penaltyAmount(150000.0)
                .remarks("Auditor appointed — fieldwork in progress")
                .isDeleted(false)
                .build(),

            // 6
            ComplianceRecord.builder()
                .title("AGM Notice Dispatch")
                .description("Dispatch of AGM notice to all shareholders")
                .sectionNumber("Section 101")
                .complianceType("Shareholder Meeting")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(7))
                .assignedTo("admin@company.com")
                .companyName("Tata Consultancy Services Ltd")
                .filingFrequency("Annual")
                .penaltyAmount(10000.0)
                .remarks("21 days notice required")
                .isDeleted(false)
                .build(),

            // 7
            ComplianceRecord.builder()
                .title("CSR Report Filing")
                .description("Corporate Social Responsibility Report")
                .sectionNumber("Section 135")
                .complianceType("CSR Compliance")
                .status(ComplianceRecord.ComplianceStatus.COMPLETED)
                .priority(ComplianceRecord.Priority.MEDIUM)
                .dueDate(LocalDate.now().minusDays(20))
                .assignedTo("manager@company.com")
                .companyName("Infosys Limited")
                .filingFrequency("Annual")
                .penaltyAmount(0.0)
                .remarks("2% of average net profit")
                .isDeleted(false)
                .build(),

            // 8
            ComplianceRecord.builder()
                .title("Secretarial Audit - MR-3")
                .description("Secretarial Audit by Company Secretary")
                .sectionNumber("Section 204")
                .complianceType("Audit")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(60))
                .assignedTo("user@company.com")
                .companyName("Wipro Limited")
                .filingFrequency("Annual")
                .penaltyAmount(200000.0)
                .remarks("Required for listed companies")
                .isDeleted(false)
                .build(),

            // 9
            ComplianceRecord.builder()
                .title("XBRL Filing")
                .description("XBRL tagging and filing of financial statements")
                .sectionNumber("MCA Circular")
                .complianceType("Annual Filing")
                .status(ComplianceRecord.ComplianceStatus.NOT_APPLICABLE)
                .priority(ComplianceRecord.Priority.LOW)
                .dueDate(LocalDate.now().plusDays(90))
                .assignedTo("admin@company.com")
                .companyName("HCL Technologies Ltd")
                .filingFrequency("Annual")
                .penaltyAmount(0.0)
                .remarks("Not applicable for this FY")
                .isDeleted(false)
                .build(),

            // 10
            ComplianceRecord.builder()
                .title("Register of Members Update")
                .description("Update and maintenance of Register of Members")
                .sectionNumber("Section 88")
                .complianceType("Statutory Register")
                .status(ComplianceRecord.ComplianceStatus.COMPLETED)
                .priority(ComplianceRecord.Priority.LOW)
                .dueDate(LocalDate.now().minusDays(30))
                .assignedTo("user@company.com")
                .companyName("Tech Mahindra Limited")
                .filingFrequency("Continuous")
                .penaltyAmount(0.0)
                .remarks("Updated after share transfer")
                .isDeleted(false)
                .build(),

            // 11
            ComplianceRecord.builder()
                .title("Dividend Declaration and Payment")
                .description("Declaration of final dividend and payment to shareholders")
                .sectionNumber("Section 123")
                .complianceType("Dividend")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(20))
                .assignedTo("manager@company.com")
                .companyName("Tata Consultancy Services Ltd")
                .filingFrequency("Annual")
                .penaltyAmount(75000.0)
                .remarks("Payment within 30 days of declaration")
                .isDeleted(false)
                .build(),

            // 12
            ComplianceRecord.builder()
                .title("Charge Registration - CHG-1")
                .description("Registration of charge created on company assets")
                .sectionNumber("Section 77")
                .complianceType("Charge")
                .status(ComplianceRecord.ComplianceStatus.OVERDUE)
                .priority(ComplianceRecord.Priority.CRITICAL)
                .dueDate(LocalDate.now().minusDays(3))
                .assignedTo("admin@company.com")
                .companyName("Infosys Limited")
                .filingFrequency("Event Based")
                .penaltyAmount(300000.0)
                .remarks("30 days from creation — URGENT")
                .isDeleted(false)
                .build(),

            // 13
            ComplianceRecord.builder()
                .title("Board Resolution for Bank Account")
                .description("Board resolution for opening new bank account")
                .sectionNumber("Section 179")
                .complianceType("Board Resolution")
                .status(ComplianceRecord.ComplianceStatus.COMPLETED)
                .priority(ComplianceRecord.Priority.LOW)
                .dueDate(LocalDate.now().minusDays(45))
                .assignedTo("user@company.com")
                .companyName("Wipro Limited")
                .filingFrequency("Event Based")
                .penaltyAmount(0.0)
                .remarks("Passed in last board meeting")
                .isDeleted(false)
                .build(),

            // 14
            ComplianceRecord.builder()
                .title("Related Party Transaction Disclosure")
                .description("Disclosure of related party transactions to Board")
                .sectionNumber("Section 188")
                .complianceType("Disclosure")
                .status(ComplianceRecord.ComplianceStatus.IN_PROGRESS)
                .priority(ComplianceRecord.Priority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(10))
                .assignedTo("manager@company.com")
                .companyName("HCL Technologies Ltd")
                .filingFrequency("Quarterly")
                .penaltyAmount(25000.0)
                .remarks("Prior approval required for material transactions")
                .isDeleted(false)
                .build(),

            // 15
            ComplianceRecord.builder()
                .title("Whistle Blower Policy Review")
                .description("Annual review of Vigil Mechanism and Whistle Blower Policy")
                .sectionNumber("Section 177")
                .complianceType("Policy Review")
                .status(ComplianceRecord.ComplianceStatus.PENDING)
                .priority(ComplianceRecord.Priority.LOW)
                .dueDate(LocalDate.now().plusDays(90))
                .assignedTo("admin@company.com")
                .companyName("Tech Mahindra Limited")
                .filingFrequency("Annual")
                .penaltyAmount(0.0)
                .remarks("Review and update policy annually")
                .isDeleted(false)
                .build()
        ));
        log.info("✅ Seeded 15 compliance records");
    }
}