package com.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringMatcherTest
{
  public static void main(String[] args)
  {
    String regexString = "(PROCEDURE|FUNCTION) Surya(.*)end Surya";
    String spamString = "visit our site\n"
    		+ " at http://10.1.1.1/. \nmore poop here.";
    
    spamString = "FUNCTION Surya My data begins with Surya and "
			+ "has lot of data in the junk that i wnat "
			+ "to exttract and print end "
			+ "end Surya";

    

    Pattern aPattern =
    Pattern.compile(regexString,Pattern.DOTALL);
    Matcher aMatcher = aPattern.matcher(spamString);
    if (aMatcher.find())
    {
      System.out.println(aMatcher.group(2));
    }
//    else
//    {
//      System.out.println("no match");
//    }
  }
}
