package esi.ma.backend.common.exception;
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}


//
//public class MLServiceException extends RuntimeException {
//    public MLServiceException(String message) {
//        super(message);
//    }
//
//    public MLServiceException(String message, Throwable cause) {
//        super(message, cause);
//    }
//}
//
//public class InvalidRequestException extends RuntimeException {
//    public InvalidRequestException(String message) {
//        super(message);
//    }
//}
//
//public class RateLimitException extends RuntimeException {
//    public RateLimitException(String message) {
//        super(message);
//    }
//}
//
//public class InvalidFileException extends RuntimeException {
//    public InvalidFileException(String message) {
//        super(message);
//    }
//}
//
//public class PollutionApiException extends RuntimeException {
//    public PollutionApiException(String message) {
//        super(message);
//    }
//}