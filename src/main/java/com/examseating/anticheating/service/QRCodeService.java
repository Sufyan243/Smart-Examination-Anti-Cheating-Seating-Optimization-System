package com.examseating.anticheating.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class QRCodeService {

    public byte[] generateSeatQRCode(String hallId, int row, int col, String rollNo) throws WriterException, IOException {
        String qrData = String.format("HALL:%s|SEAT:%d,%d|STUDENT:%s", hallId, row, col, rollNo);
        return generateQRCode(qrData, 200, 200);
    }

    public byte[] generateAdmitCardQRCode(String rollNo, String name, String hallId, int row, int col) throws WriterException, IOException {
        String qrData = String.format("ROLL:%s|NAME:%s|HALL:%s|SEAT:%d,%d", rollNo, name, hallId, row, col);
        return generateQRCode(qrData, 150, 150);
    }

    public byte[] generateHallQRCode(String hallId, int rows, int cols) throws WriterException, IOException {
        String qrData = String.format("HALL:%s|CAPACITY:%dx%d", hallId, rows, cols);
        return generateQRCode(qrData, 150, 150);
    }

    private byte[] generateQRCode(String data, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        
        log.debug("Generated QR code for data: {}", data);
        return outputStream.toByteArray();
    }
}
