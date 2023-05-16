namespace NS {
	int g_x;

}

int UUT(int x)
{
	x += 5;
	return NS::STUB(x);
}


