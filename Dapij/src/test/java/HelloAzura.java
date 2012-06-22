public class HelloAzura {
  
  //This has to be compiled and put in the build/classes/dapij directory
  
  public static int square(int input)
  {
    return input*input;
  }
  
  public static void azura()
  {
	  //Azura is the name of my dog
	  System.out.println("Hello Azura");
  }
	
  public static void main(String args[]) {
	  
	System.out.println("The square of 5 is " + square(5));
    azura();
  }
}

