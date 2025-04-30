package policyservice.util;

import policyservice.dto.PolicyDto;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class PdfGeneratorUtil {

    public byte[] generatePolicyPdf(PolicyDto policyDto /*, PlanDto planDto, UserDto userDto */) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Basic PDF Content - Replace with detailed policy information
        document.add(new Paragraph("Health Insurance Policy Document"));
        document.add(new Paragraph("Policy Number: " + policyDto.getPolicyNumber()));
        document.add(new Paragraph("Status: " + policyDto.getPolicyStatus()));
        document.add(new Paragraph("Effective Date: " + policyDto.getEffectiveDate()));
        document.add(new Paragraph("Expiration Date: " + policyDto.getExpirationDate()));
        document.add(new Paragraph("Premium: " + policyDto.getPremiumAmount() + " (" + policyDto.getPaymentFrequency() + ")"));

        if (policyDto.getPolicyHolder() != null) {
            document.add(new Paragraph("\nPolicy Holder:"));
            document.add(new Paragraph("  Name: " + policyDto.getPolicyHolder().getFirstName() + " " + policyDto.getPolicyHolder().getLastName()));
            document.add(new Paragraph("  DOB: " + policyDto.getPolicyHolder().getDateOfBirth()));
        }

        if (policyDto.getDependents() != null && !policyDto.getDependents().isEmpty()) {
            document.add(new Paragraph("\nDependents:"));
            policyDto.getDependents().forEach(dep -> {
                document.add(new Paragraph("  Name: " + dep.getFirstName() + " " + dep.getLastName() + " (Relationship: " + dep.getRelationshipToPolicyHolder() + ")"));
                document.add(new Paragraph("    DOB: " + dep.getDateOfBirth()));
            });
        }

        // TODO: Add Plan Details (requires fetching from Plan Service or having data)
        // document.add(new Paragraph("\nPlan Details:"));
        // document.add(new Paragraph("  Plan Name: " + planDto.getPlanName()));
        // ... add more plan details ...

        document.close();

        return baos.toByteArray();
    }
}

