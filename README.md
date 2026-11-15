# BWAYO GSM PRO

Native Android starter project for phone service utilities.

Included:
- USB host device detection (OTG)
- Firmware file selection using Android's document picker
- MTK / Qualcomm / Unisoc chipset selector
- Basic device information
- ADB / Fastboot guidance
- Authorized FRP recovery guidance
- Activity log

## Build using GitHub Actions
The workflow at `.github/workflows/android.yml` builds a debug APK on pushes to main or manual dispatch. Open the repository's Actions tab, select the workflow, and download the `BWAYO-GSM-PRO-debug-apk` artifact after a successful run.

## Important limitations
This is not a complete commercial flashing suite. USB detection and file selection are implemented. No low-level firmware write/erase operation is implemented. Actual MTK, Qualcomm and Unisoc flashing requires validated chipset-specific protocols, compatible loaders, exact model checks, and hardware testing.

FRP assistance is limited to official account recovery and authorized service procedures; no security bypass is provided.
