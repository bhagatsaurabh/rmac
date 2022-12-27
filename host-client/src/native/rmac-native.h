#pragma once

#ifdef RMACNATIVE_EXPORTS
#define RMACNATIVE_API __declspec(dllexport)
#else
#define RMACNATIVE_API __declspec(dllimport)
#endif

extern "C" RMACNATIVE_API bool isMicActive(char *micName);