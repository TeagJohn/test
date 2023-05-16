#include "header.h" 


bool SourcePatern_TC048(unsigned int va)
{
       /* signed int 32 bit: -2147483648 ~ 2147483647 */
       /* unsigned int 32 bit: 0 ~ 4294967295 */
       if(va > 2147483647 )
      {

          return true;
      }
      else
      {

          return false;
      }
}


