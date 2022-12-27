#include <windows.h>
#include "rmac-native.h"
#include <mmdeviceapi.h>
#include <audiopolicy.h>
#include <initguid.h>
#include <devpkey.h>
#include <Functiondiscoverykeys_devpkey.h>
#include <string>
#include <iostream>
#include <objbase.h>
#include <codecvt>
#include <locale>

#define SAFE_RELEASE(p)     \
    {                       \
        if ((p))            \
        {                   \
            (p)->Release(); \
            (p) = 0;        \
        }                   \
    }

bool isMicActive(char *micName)
{
    // Get the audio endpoint associated with the microphone device
    HRESULT hr = S_OK;
    IMMDeviceEnumerator *pEnumerator = NULL;
    IAudioSessionManager2 *pSessionManager = NULL;
    BOOL result = FALSE;

    CoInitialize(0);

    // Create the device enumerator.
    hr = CoCreateInstance(
        __uuidof(MMDeviceEnumerator),
        NULL, CLSCTX_ALL,
        __uuidof(IMMDeviceEnumerator),
        (void **)&pEnumerator);

    IMMDeviceCollection *dCol = NULL;
    hr = pEnumerator->EnumAudioEndpoints(eCapture, DEVICE_STATE_ACTIVE, &dCol);
    UINT dCount;
    hr = dCol->GetCount(&dCount);
    for (UINT i = 0; i < dCount; i++)
    {
        IMMDevice *pCaptureDevice = NULL;
        hr = dCol->Item(i, &pCaptureDevice);

        IPropertyStore *pProps = NULL;
        hr = pCaptureDevice->OpenPropertyStore(
            STGM_READ, &pProps);

        PROPVARIANT varName;
        // Initialize container for property value.
        PropVariantInit(&varName);

        // Get the endpoint's friendly-name property.
        hr = pProps->GetValue(
            PKEY_Device_FriendlyName, &varName);

        std::wstring nameStr(varName.pwszVal);

        // Determine whether it is the microphone device you are focusing on
        std::wstring_convert<std::codecvt_utf8<wchar_t>, wchar_t> converter;
        std::wstring value = converter.from_bytes(micName);
        std::size_t found = nameStr.find(value);
        if (found != std::string::npos)
        {
            // Get the session manager.
            hr = pCaptureDevice->Activate(
                __uuidof(IAudioSessionManager2), CLSCTX_ALL,
                NULL, (void **)&pSessionManager);
            break;
        }
    }

    // Get session state
    if (!pSessionManager)
    {
        return (result = FALSE);
    }

    int cbSessionCount = 0;
    LPWSTR pswSession = NULL;

    IAudioSessionEnumerator *pSessionList = NULL;
    IAudioSessionControl *pSessionControl = NULL;
    IAudioSessionControl2 *pSessionControl2 = NULL;

    // Get the current list of sessions.
    hr = pSessionManager->GetSessionEnumerator(&pSessionList);

    // Get the session count.
    hr = pSessionList->GetCount(&cbSessionCount);

    for (int index = 0; index < cbSessionCount; index++)
    {
        CoTaskMemFree(pswSession);
        SAFE_RELEASE(pSessionControl);

        // Get the <n>th session.
        hr = pSessionList->GetSession(index, &pSessionControl);

        hr = pSessionControl->QueryInterface(
            __uuidof(IAudioSessionControl2), (void **)&pSessionControl2);

        // Exclude system sound session
        hr = pSessionControl2->IsSystemSoundsSession();
        if (S_OK == hr)
        {
            continue;
        }

        // Optional. Determine which application is using Microphone for recording
        LPWSTR instId = NULL;
        hr = pSessionControl2->GetSessionInstanceIdentifier(&instId);
        if (S_OK == hr)
        {
            // wprintf_s(L"SessionInstanceIdentifier: %s\n", instId);
        }

        AudioSessionState state;
        hr = pSessionControl->GetState(&state);
        switch (state)
        {
        case AudioSessionStateInactive:
            break;
        case AudioSessionStateActive:
            result = TRUE;
            break;
        case AudioSessionStateExpired:
            break;
        }
    }

done:
    // Clean up.
    SAFE_RELEASE(pSessionControl);
    SAFE_RELEASE(pSessionList);
    SAFE_RELEASE(pSessionControl2);
    SAFE_RELEASE(pSessionManager);
    SAFE_RELEASE(pEnumerator);

    return result != 0;
}