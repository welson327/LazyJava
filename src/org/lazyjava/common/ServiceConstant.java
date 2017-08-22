package org.lazyjava.common;

public class ServiceConstant {
	// proj package name
	public static final String PROJ_NAME = "lazyjava";
	
	// top path
	public static final String YIABI_DATA_TOP_PATH = "/lazyjava";
		
    // success/err
    public static final int SUCCESS           	= 200;
    public static final int DB_ERR              = 300;
    public static final int INPUT_ERR           = 400;
    public static final int INPUT_CONTAINS_EMOJI= 401;
    public static final int FAIL 				= 500;
    public static final int AUTHENTICATION_FAIL = 501;
    public static final int TRANSACTION_FAIL    = 502;    
    public static final int PASSWORD_INCORRECT  = 503;    
    public static final int UNKNOWN_ERR  		= 555;    
    public static final int IO_ERR     		    = 600;
    
    // not found
    public static final int DATA_NOT_FOUND   		= 700;
    public static final int FILE_NOT_FOUND   		= 701;
    public static final int ACCOUNT_NOT_FOUND   	= 702;
    public static final int SERVICE_NOT_FOUND   	= 703;
    public static final int BOOK_NOT_PUBLIC   		= 704;
    public static final int BOOK_REMOVED_BY_OWNER	= 705;
    public static final int PAGE_NOT_FOUND			= 706;
    public static final int QUESTION_NOT_FOUND		= 707;
    public static final int NGRAM_NOT_PREPARED		= 708;
    public static final int IM_ID_NOT_DEFINED		= 709;
    public static final int OA_NAME_NOT_DEFINED		= 710;
    public static final int UNKNOWN_HOST 			= 750;
    
    // is exist
    public static final int DATA_FOUND   	  = 800;
    public static final int FILE_FOUND   	  = 801;
    public static final int ACCOUNT_FOUND     = 802;
    public static final int NICKNAME_FOUND    = 803;
    public static final int MOBILEPHONE_FOUND = 804;
    public static final int HAS_BEEN_ON_SHELF = 805;
    public static final int BLACKLIST_FOUND   = 806;
    public static final int PASSWORD_FOUND	  = 807;
    public static final int ACCOUNT_NICKNAME_MOBILE_FOUND 	= 811;
    public static final int ACCOUNT_NICKNAME_FOUND 			= 812;
    public static final int ACCOUNT_MOBILE_FOUND 			= 813;
    public static final int NICKNAME_MOBILE_FOUND 			= 814;
    public static final int HAS_DOWNLOADED                  = 815;
    
    public static final int RESERVED_WORD     = 899;
    
    // token
    public static final int TOKEN_IS_NULL 	= 997;
    public static final int TOKEN_EXPIRED 	= 998;
    public static final int TOKEN_MISMATCH 	= 999;
    
    // email & password
    public static final int PASSWORD_SHOULD_ALPHANUMERIC = 1001;
    public static final int PASSWORD_LEN_ERROR = 1002;
    public static final int PASSWORD_AT_LEAST_ONE_ALPHA = 1003;
    public static final int PASSWORD_AT_LEAST_ONE_DIGIT = 1004;
    public static final int PASSWORD_AT_LEAST_SPECIAL_SYMBOL = 1005;
    public static final int EMAIL_FORMAT_ERROR = 1100;
    public static final int NICKNAME_FORMAT_ERROR = 1101;
    public static final int EMAIL_TOO_LONG = 1102;
    
    // other
    public static final int PLS_CONTANT_CUSTOMER_SERVICE = 9000;
    public static final int USE_DEFAULT_COVER_IMAGE = 9001;
    public static final int PERMISSION_DENY = 9002;
    public static final int SYSTEM_IS_BUSY = 9003;
    public static final int FILE_TOO_LARGE = 9004;
    public static final int PAGESIZE_TOO_LARGE = 9005;
    public static final int NOT_ACTIVATED = 9006;
    public static final int NOT_UTF8 = 9007;
    public static final int OVER_PAGES_LIMITATION = 9008;
    public static final int OVER_CHAR_LIMITATION = 9009;
    
}
