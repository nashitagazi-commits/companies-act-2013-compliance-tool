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
        userRepository.saveAll(List.of(
            User.builder().fullName("Admin User")
                .email("admin@company.com")
                .password(passwordEncoder.encode("admin123"))
                .role(User.Role.ADMIN).isActive(true).build(),
            User.builder().fullName("Manager User")
                .email("manager@company.com")
                .password(passwordEncoder.encode("manager123"))
                .role(User.Role.MANAGER).isActive(true).build(),
            User.builder().fullName("Regular User")
                .email("user@company.com")
                .password(passwordEncoder.encode("user123"))
                .role(User.Role.USER).isActive(true).build()
        ));
        log.info("✅ Seeded 3 users");
    }

    private void seedComplianceRecords() {
        if (complianceRecordRepository.count() > 0) {
            log.info("Records already seeded — skipping");
            return;
        }
        complianceRecordRepository.saveAll(List.of(
            build("Annual Return Filing - MGT-7", "Section 92",
                "Annual Filing", ComplianceRecord.ComplianceStatus.PENDING,
                ComplianceRecord.Priority.HIGH, 15,
                "Tata Consultancy Services Ltd", 50000.0),
            build("Financial Statements - AOC-4", "Section 137",
                "Annual Filing", ComplianceRecord.ComplianceStatus.IN_PROGRESS,
                ComplianceRecord.Priority.HIGH, 30,
                "Infosys Limited", 100000.0),
            build("Board Meeting Q1", "Section 173",
                "Board Meeting", ComplianceRecord.ComplianceStatus.COMPLETED,
                ComplianceRecord.Priority.MEDIUM, -10,
                "Wipro Limited", 25000.0),
            build("Director KYC - DIR-3", "Rule 12A",
                "Director Compliance", ComplianceRecord.ComplianceStatus.OVERDUE,
                ComplianceRecord.Priority.CRITICAL, -5,
                "HCL Technologies Ltd", 5000.0),
            build("Statutory Audit FY 2025-26", "Section 139",
                "Audit", ComplianceRecord.ComplianceStatus.IN_PROGRESS,
                ComplianceRecord.Priority.HIGH, 45,
                "Tech Mahindra Limited", 150000.0),
            build("AGM Notice Dispatch", "Section 101",
                "Shareholder Meeting", ComplianceRecord.ComplianceStatus.PENDING,
                ComplianceRecord.Priority.HIGH, 7,
                "Tata Consultancy Services Ltd", 10000.0),
            build("CSR Report Filing", "Section 135",
                "CSR Compliance", ComplianceRecord.ComplianceStatus.COMPLETED,
                ComplianceRecord.Priority.MEDIUM, -20,
                "Infosys Limited", 0.0),
            build("Secretarial Audit - MR-3", "Section 204",
                "Audit", ComplianceRecord.ComplianceStatus.PENDING,
                ComplianceRecord.Priority.MEDIUM, 60,
                "Wipro Limited", 200000.0),
            build("XBRL Filing", "MCA Circular",
                "Annual Filing", ComplianceRecord.ComplianceStatus.NOT_APPLICABLE,
                ComplianceRecord.Priority.LOW, 90,
                "HCL Technologies Ltd", 0.0),
            build("Register of Members Update", "Section 88",
                "Statutory Register", ComplianceRecord.ComplianceStatus.COMPLETED,
                ComplianceRecord.Priority.LOW, -30,
                "Tech Mahindra Limited", 0.0),
            build("Dividend Declaration", "Section 123",
                "Dividend", ComplianceRecord.ComplianceStatus.PENDING,
                ComplianceRecord.Priority.HIGH, 20,
                "Tata Consultancy Services Ltd", 75000.0),
            build("Charge Registration - CHG-1", "Section 77",
                "Charge", ComplianceRecord.ComplianceStatus.OVERDUE,
                ComplianceRecord.Priority.CRITICAL, -3,
                "Infosys Limited", 300000.0),
            build("Board Resolution Bank Account", "Section 179",
                "Board Resolution", ComplianceRecord.ComplianceStatus.COMPLETED,
                ComplianceRecord.Priority.LOW, -45,
                "Wipro Limited", 0.0),
            build("Related Party Transaction", "Section 188",
                "Disclosure", ComplianceRecord.ComplianceStatus.IN_PROGRESS,
                ComplianceRecord.Priority.MEDIUM, 10,
                "HCL Technologies Ltd", 25000.0),
            build("Whistle Blower Policy Review", "Section 177",
                "Policy Review", ComplianceRecord.ComplianceStatus.PENDING,
                ComplianceRecord.Priority.LOW, 90,
                "Tech Mahindra Limited", 0.0),
            build("Form INC-20A Filing", "Section 10A",
                "Commencement Filing", ComplianceRecord.ComplianceStatus.COMPLETED,
                ComplianceRecord.Priority.HIGH, -60,
                "Reliance Industries Ltd", 50000.0),
            build("Statutory Registers Maintenance", "Section 85",
                "Statutory Register", ComplianceRecord.ComplianceStatus.IN_PROGRESS,
                ComplianceRecord.Priority.MEDIUM, 5,
                "Bajaj Auto Limited", 0.0),
            build("Board Meeting Q2", "Section 173",
                "Board Meeting", ComplianceRecord.ComplianceStatus.PENDING,
                ComplianceRecord.Priority.HIGH, 25,
                "Maruti Suzuki India Ltd", 25000.0),
            build("Annual General Meeting", "Section 96",
                "Shareholder Meeting", ComplianceRecord.ComplianceStatus.PENDING,
                ComplianceRecord.Priority.HIGH, 35,
                "Tata Motors Limited", 100000.0),
            build("DPT-3 Return Filing", "Section 73",
                "Deposit Return", ComplianceRecord.ComplianceStatus.OVERDUE,
                ComplianceRecord.Priority.CRITICAL, -2,
                "Larsen and Toubro Ltd", 500000.0),
            build("MGT-14 Filing", "Section 94",
                "Board Resolutions", ComplianceRecord.ComplianceStatus.PENDING,
                ComplianceRecord.Priority.MEDIUM, 12,
                "Reliance Industries Ltd", 10000.0),
            build("Auditor Appointment - ADT-1", "Section 139",
                "Auditor", ComplianceRecord.ComplianceStatus.COMPLETED,
                ComplianceRecord.Priority.HIGH, -90,
                "Bajaj Auto Limited", 0.0),
            build("MSME Payment Compliance", "MSMED Act",
                "Payment Compliance", ComplianceRecord.ComplianceStatus.IN_PROGRESS,
                ComplianceRecord.Priority.HIGH, 8,
                "Maruti Suzuki India Ltd", 0.0),
            build("PF and ESI Compliance", "Labour Laws",
                "Labour Compliance", ComplianceRecord.ComplianceStatus.COMPLETED,
                ComplianceRecord.Priority.HIGH, -5,
                "Tata Motors Limited", 0.0),
            build("GST Annual Return GSTR-9", "GST Act",
                "Tax Compliance", ComplianceRecord.ComplianceStatus.PENDING,
                ComplianceRecord.Priority.HIGH, 40,
                "Larsen and Toubro Ltd", 50000.0),
            build("Income Tax Return Filing", "IT Act",
                "Tax Compliance", ComplianceRecord.ComplianceStatus.IN_PROGRESS,
                ComplianceRecord.Priority.HIGH, 50,
                "Reliance Industries Ltd", 100000.0),
            build("Transfer Pricing Report", "Section 92E",
                "Tax Compliance", ComplianceRecord.ComplianceStatus.PENDING,
                ComplianceRecord.Priority.MEDIUM, 55,
                "Bajaj Auto Limited", 25000.0),
            build("Board Diversity Policy", "SEBI LODR",
                "Policy Compliance", ComplianceRecord.ComplianceStatus.COMPLETED,
                ComplianceRecord.Priority.LOW, -15,
                "Maruti Suzuki India Ltd", 0.0),
            build("Insider Trading Policy Update", "SEBI PIT",
                "SEBI Compliance", ComplianceRecord.ComplianceStatus.PENDING,
                ComplianceRecord.Priority.HIGH, 18,
                "Tata Motors Limited", 0.0),
            build("Risk Management Policy Review", "SEBI LODR",
                "Policy Review", ComplianceRecord.ComplianceStatus.IN_PROGRESS,
                ComplianceRecord.Priority.MEDIUM, 22,
                "Larsen and Toubro Ltd", 0.0)
        ));
        log.info("✅ Seeded 30 compliance records");
    }

    private ComplianceRecord build(String title, String section,
            String type, ComplianceRecord.ComplianceStatus status,
            ComplianceRecord.Priority priority, int dueDays,
            String company, Double penalty) {
        return ComplianceRecord.builder()
                .title(title)
                .sectionNumber(section)
                .complianceType(type)
                .status(status)
                .priority(priority)
                .dueDate(LocalDate.now().plusDays(dueDays))
                .companyName(company)
                .penaltyAmount(penalty)
                .assignedTo("manager@company.com")
                .isDeleted(false)
                .build();
    }
}