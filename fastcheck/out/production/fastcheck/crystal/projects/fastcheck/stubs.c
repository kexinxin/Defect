typedef unsigned int size_t;

void *memcpy (void * dest, const void * src, size_t n) 
{
    return dest;
}

void *memmove (void *dest, const void *src, size_t n) 
{
    return dest;
}

void *memccpy (void * dest, const void * src, int c, size_t n)
{
    return dest;
}

void *memset (void *s, int c, size_t n) 
{
    return s;
}

void *memchr (const void *s, int c, size_t n)
{
    return s;
}

char *strcpy (char * dest, const char * src)
{
    return dest;
}

char *strncpy (char * dest, const char * src, size_t n)
{
    return dest;
}

char *strcat (char * dest, const char * src)
{
    return dest;
}

char *strncat (char * dest, const char * src, size_t n)
{
    return dest;
}

char *strdup (const char *s) 
{
    char *t = malloc(sizeof(char*) * 10);
    return t;
}

char *strndup(const char *s, size_t n)
{
    char *t = malloc(sizeof(char*) * 10);
    return t;
}

char *strdupa(const char *s)
{
    char *t = alloca(sizeof(char*) * 10);
    return t;
}

char *strndupa(const char *s, size_t n)
{
    char *t = alloca(sizeof(char*) * n);
    return t;
}


char *strsep (char ** stringp, const char * delim) 
{
    return *stringp;
}
