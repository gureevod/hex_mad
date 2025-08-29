package com.company.hex.testing.allure;

import com.company.hex.core.logging.HexLoggerFactory;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Утилитарный класс для работы с вложениями в Allure отчетах.
 * Предоставляет методы для прикрепления скриншотов, логов и других файлов.
 * 
 * @author Hex Framework
 * @version 1.0
 * @since 1.0
 */
public class AllureAttachmentHelper {
    
    private static final Logger logger = HexLoggerFactory.getUtilLogger(AllureAttachmentHelper.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private AllureAttachmentHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Прикрепляет скриншот к Allure отчету.
     * 
     * @param screenshotBytes байты скриншота
     * @param name название вложения
     * @return байты скриншота для цепочки вызовов
     */
    @Attachment(value = "{name}", type = "image/png")
    public static byte[] attachScreenshot(byte[] screenshotBytes, String name) {
        if (screenshotBytes == null || screenshotBytes.length == 0) {
            logger.warn("Attempted to attach empty screenshot: {}", name);
            return new byte[0];
        }
        
        logger.debug("Attaching screenshot: {} (size: {} bytes)", name, screenshotBytes.length);
        return screenshotBytes;
    }
    
    /**
     * Прикрепляет скриншот с автоматическим именем.
     * 
     * @param screenshotBytes байты скриншота
     * @return байты скриншота для цепочки вызовов
     */
    public static byte[] attachScreenshot(byte[] screenshotBytes) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        return attachScreenshot(screenshotBytes, "Screenshot_" + timestamp);
    }
    
    /**
     * Прикрепляет скриншот из файла.
     * 
     * @param screenshotPath путь к файлу скриншота
     * @param name название вложения
     * @return true если успешно прикреплен, false в противном случае
     */
    public static boolean attachScreenshotFromFile(String screenshotPath, String name) {
        try {
            Path path = Paths.get(screenshotPath);
            if (!Files.exists(path)) {
                logger.warn("Screenshot file not found: {}", screenshotPath);
                return false;
            }
            
            byte[] screenshotBytes = Files.readAllBytes(path);
            attachScreenshot(screenshotBytes, name);
            logger.debug("Successfully attached screenshot from file: {}", screenshotPath);
            return true;
            
        } catch (IOException e) {
            logger.error("Failed to attach screenshot from file: {} - Error: {}", screenshotPath, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Прикрепляет текстовый лог к Allure отчету.
     * 
     * @param logContent содержимое лога
     * @param name название вложения
     * @return содержимое лога для цепочки вызовов
     */
    @Attachment(value = "{name}", type = "text/plain")
    public static String attachTextLog(String logContent, String name) {
        if (logContent == null || logContent.trim().isEmpty()) {
            logger.warn("Attempted to attach empty log: {}", name);
            return "Log content is empty";
        }
        
        logger.debug("Attaching text log: {} (length: {} characters)", name, logContent.length());
        return logContent;
    }
    
    /**
     * Прикрепляет лог с автоматическим именем.
     * 
     * @param logContent содержимое лога
     * @return содержимое лога для цепочки вызовов
     */
    public static String attachTextLog(String logContent) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        return attachTextLog(logContent, "Log_" + timestamp);
    }
    
    /**
     * Прикрепляет лог из файла.
     * 
     * @param logFilePath путь к файлу лога
     * @param name название вложения
     * @return true если успешно прикреплен, false в противном случае
     */
    public static boolean attachLogFromFile(String logFilePath, String name) {
        try {
            Path path = Paths.get(logFilePath);
            if (!Files.exists(path)) {
                logger.warn("Log file not found: {}", logFilePath);
                return false;
            }
            
            String logContent = Files.readString(path);
            attachTextLog(logContent, name);
            logger.debug("Successfully attached log from file: {}", logFilePath);
            return true;
            
        } catch (IOException e) {
            logger.error("Failed to attach log from file: {} - Error: {}", logFilePath, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Прикрепляет JSON данные к Allure отчету.
     * 
     * @param jsonContent JSON содержимое
     * @param name название вложения
     * @return JSON содержимое для цепочки вызовов
     */
    @Attachment(value = "{name}", type = "application/json")
    public static String attachJson(String jsonContent, String name) {
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            logger.warn("Attempted to attach empty JSON: {}", name);
            return "{}";
        }
        
        logger.debug("Attaching JSON: {} (length: {} characters)", name, jsonContent.length());
        return jsonContent;
    }
    
    /**
     * Прикрепляет XML данные к Allure отчету.
     * 
     * @param xmlContent XML содержимое
     * @param name название вложения
     * @return XML содержимое для цепочки вызовов
     */
    @Attachment(value = "{name}", type = "application/xml")
    public static String attachXml(String xmlContent, String name) {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            logger.warn("Attempted to attach empty XML: {}", name);
            return "<empty/>";
        }
        
        logger.debug("Attaching XML: {} (length: {} characters)", name, xmlContent.length());
        return xmlContent;
    }
    
    /**
     * Прикрепляет HTML данные к Allure отчету.
     * 
     * @param htmlContent HTML содержимое
     * @param name название вложения
     * @return HTML содержимое для цепочки вызовов
     */
    @Attachment(value = "{name}", type = "text/html")
    public static String attachHtml(String htmlContent, String name) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            logger.warn("Attempted to attach empty HTML: {}", name);
            return "<html><body>Content is empty</body></html>";
        }
        
        logger.debug("Attaching HTML: {} (length: {} characters)", name, htmlContent.length());
        return htmlContent;
    }
    
    /**
     * Прикрепляет произвольный файл к Allure отчету.
     * 
     * @param filePath путь к файлу
     * @param name название вложения
     * @param mimeType MIME тип файла
     * @return true если успешно прикреплен, false в противном случае
     */
    public static boolean attachFile(String filePath, String name, String mimeType) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                logger.warn("File not found: {}", filePath);
                return false;
            }
            
            byte[] fileBytes = Files.readAllBytes(path);
            Allure.addAttachment(name, mimeType, new ByteArrayInputStream(fileBytes), getFileExtension(filePath));
            
            logger.debug("Successfully attached file: {} as {} (size: {} bytes)", filePath, name, fileBytes.length);
            return true;
            
        } catch (IOException e) {
            logger.error("Failed to attach file: {} - Error: {}", filePath, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Прикрепляет видео к Allure отчету.
     * 
     * @param videoBytes байты видео
     * @param name название вложения
     * @return байты видео для цепочки вызовов
     */
    @Attachment(value = "{name}", type = "video/mp4")
    public static byte[] attachVideo(byte[] videoBytes, String name) {
        if (videoBytes == null || videoBytes.length == 0) {
            logger.warn("Attempted to attach empty video: {}", name);
            return new byte[0];
        }
        
        logger.debug("Attaching video: {} (size: {} bytes)", name, videoBytes.length);
        return videoBytes;
    }
    
    /**
     * Прикрепляет CSV данные к Allure отчету.
     * 
     * @param csvContent CSV содержимое
     * @param name название вложения
     * @return CSV содержимое для цепочки вызовов
     */
    @Attachment(value = "{name}", type = "text/csv")
    public static String attachCsv(String csvContent, String name) {
        if (csvContent == null || csvContent.trim().isEmpty()) {
            logger.warn("Attempted to attach empty CSV: {}", name);
            return "No data";
        }
        
        logger.debug("Attaching CSV: {} (length: {} characters)", name, csvContent.length());
        return csvContent;
    }
    
    /**
     * Создает и прикрепляет отчет об ошибке.
     * 
     * @param exception исключение
     * @param additionalInfo дополнительная информация
     */
    public static void attachErrorReport(Throwable exception, String additionalInfo) {
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("=== ERROR REPORT ===\n");
        errorReport.append("Timestamp: ").append(LocalDateTime.now()).append("\n");
        errorReport.append("Exception Type: ").append(exception.getClass().getSimpleName()).append("\n");
        errorReport.append("Message: ").append(exception.getMessage()).append("\n");
        
        if (additionalInfo != null && !additionalInfo.trim().isEmpty()) {
            errorReport.append("Additional Info: ").append(additionalInfo).append("\n");
        }
        
        errorReport.append("\n=== STACK TRACE ===\n");
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        errorReport.append(sw.toString());
        
        attachTextLog(errorReport.toString(), "Error_Report_" + LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }
    
    /**
     * Получает расширение файла из пути.
     * 
     * @param filePath путь к файлу
     * @return расширение файла
     */
    private static String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filePath.length() - 1) {
            return "";
        }
        
        return filePath.substring(lastDotIndex + 1);
    }
}