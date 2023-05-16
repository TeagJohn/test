//============================================================================
// Name        : main.c
// Author      : 
// Version     :
// Copyright   : Vinh Pham
// Description : Source sample for AkaUT
//============================================================================

#include <stdio.h>

int main(void) ;
char CharParamPlusConst(char va);
int IntParamPlusConst(int va);
char CharGlobalVarPlusConst(void);
int IntGlobalVarPlusConst(void);
char CharParamPlusParam(char va,char vb);
char CharGlobalVarPlusGlobalVar(void);
char CharIncrement(char vd);

char vA_chr, vB_chr;
int vA_int;
 
int main(void) 
{
	char va, vb, vd, ve;
	int vc;
	
	vc = CharParamPlusConst(va);
	vc = IntParamPlusConst(va);
	
	vc = CharGlobalVarPlusConst();
	vc = IntGlobalVarPlusConst();

	
	vc = CharParamPlusParam(va, vb);
	vc = CharGlobalVarPlusGlobalVar();
	ve = CharIncrement(vd);
	return 0;
}

char CharParamPlusConst(char va)
{
  return va + 5;
}

int IntParamPlusConst(int va)
{
  return va + 5;
}

char CharGlobalVarPlusConst(void)
{
  return vA_chr + 5;
}

int IntGlobalVarPlusConst(void)
{
  return vA_int + 5;
}

char CharParamPlusParam(char va,char vb)
{
  return va + vb;
}

char CharGlobalVarPlusGlobalVar(void)
{
  return vA_chr + vB_chr;
}

char CharIncrement(char vd)
{
	if(vd < 255)
	{
        return vd + 1;
    }
    else
    {
        return 130;
    }
}

