# Pleos Connect 앱 개발 시작하기 (요약)

원문: https://document.pleos.ai/docs/pleos-only/getting-started
수집일: 2026-10-25 · 원문을 정리한 요약이며 예제 코드 전문은 원문 참고

Android Studio 프로젝트 생성 → Pleos Connect Emulator 실행 → 첫 Vehicle SDK 호출까지의 빠른 시작 가이드.

## 사전 준비
- Android Studio
- Android SDK API 34
- `minSdk` API 28 이상
- Pleos Connect Emulator 시스템 이미지

## 1. Android 프로젝트 만들기
- **File > New > New Project**
- **Car** 또는 **Automotive** 템플릿에서 **Empty Activity** 선택
- 언어 **Kotlin**, **Minimum SDK** API 28 이상
- `app` 모듈이 빌드되는지 확인

## 2. Pleos Connect Emulator 설치 및 구성
1. **Tools > SDK Manager > SDK Update Sites**에 저장소 추가
   - **Name**: `Pleos Connect System Image`
   - **URL**: 안내받은 Pleos Connect Emulator 다운로드 URL
2. API 34 Pleos Connect 시스템 이미지 설치 후 Android Studio 재시작
3. **Tools > Device Manager**에서 **Automotive** 기기 생성
   - **Boot option**: Cold boot
   - **RAM**: 4 GB
   - **System image**: API 34 Pleos Connect 이미지
4. AVD 실행

자세히: [Pleos Connect Emulator 설치 및 실행](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)

## 3. Vehicle SDK 가져오기

### Maven 저장소 등록 (`settings.gradle.kts`)
- `google()`
- `mavenCentral()`
- `https://nexus-playground.pleos.ai/repository/maven-releases/`

### SDK 의존성 추가 (`app/build.gradle.kts`의 `dependencies`)
- `ai.pleos.playground:Vehicle:2.3.1`

이후 **Sync Now**. 자세히: [Importing Vehicle SDK](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)

## 4. 차량 권한 선언
예제는 문 위치를 읽으므로 `app/src/main/AndroidManifest.xml`에 `pleos.car.permission.CAR_DOORS` 선언.
API별 권한: [Vehicle SDK permissions](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)

## 5. 첫 번째 API 호출 흐름
1. context로 `Vehicle` 객체를 만들고 `initialize()` 호출
2. `getDoor()`로 Door 도메인 객체 획득
3. `DoorArea(DoorValue.ROW_1_LEFT)`로 `getCurrentDoorPosition()` 호출 — 성공은 `onComplete`, 오류는 `onFailed`에서 처리
4. `release()`는 요청 직후가 아니라 SDK가 더 이상 필요 없을 때(예: `onDestroy()`) 호출

두 콜백 모두 비동기.

## 6. 에뮬레이터에서 확인
1. 에뮬레이터 부팅 완료 확인
2. 실행 대상으로 Pleos Connect Emulator 선택
3. 빌드·실행
4. 문 위치 결과가 처리되는지 확인

값이 안 나오면 확인할 것:
- `minSdk` 28 이상인지
- `pleos.car.permission.CAR_DOORS` 선언했는지
- Gradle sync 성공했는지
- 에뮬레이터 실행 중인지
- `onFailed`로 들어온 예외 메시지

## 다음 단계
1. [Pleos Connect 앱 계획 및 설계 가이드](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/intro): 화면, 주행 사용성, 성능, 개인정보
2. [Pleos Connect 앱 개발 가이드](https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/intro): 기능 구현
3. [Pleos Connect SDK 및 API 문서](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/): SDK 선택, API 레퍼런스
4. [Vehicle SDK 권한](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission), [Capability API](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability), [Get API](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
5. [Vehicle SDK Sample App 사용 가이드](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app)
6. [Pleos Connect Virtual Console 사용 가이드](https://document.pleos.ai/docs/pleos-only/app-test-guide/virtual-console-guide): 차량 상태·앱 연동 테스트
7. [Pleos Connect 앱 등록 및 배포 가이드](https://document.pleos.ai/docs/pleos-only/distribute/app-submission-guide): Pleos App Market 심사·제출
