name: "🐛 Bug Report"
description: "Сообщить об ошибке в работе приложения"
title: "[Bug] Краткое описание проблемы"
labels: ["type: bug", "status: new"]
assignees: ""
body:
  - type: markdown
    attributes:
      value: |
        ## 🐛 Сообщение об ошибке
        Пожалуйста, заполните эту форму, чтобы помочь нам воспроизвести и исправить проблему.
  - type: input
    id: version
    attributes:
      label: "Версия приложения"
      description: "Какую версию приложения вы используете?"
      placeholder: "например, 1.2.3"
    validations:
      required: true
  - type: input
    id: environment
    attributes:
      label: "Окружение"
      description: "ОС, устройство, браузер"
      placeholder: "Windows 11, Chrome 120"
    validations:
      required: true
  - type: textarea
    id: description
    attributes:
      label: "Описание проблемы"
      description: "Четкое и ясное описание того, что происходит"
      placeholder: "При нажатии на кнопку X происходит Y вместо Z"
    validations:
      required: true
  - type: textarea
    id: steps
    attributes:
      label: "Шаги воспроизведения"
      description: "Пошаговое описание как воспроизвести проблему"
      value: |
        1. 
        2. 
        3. 
      render: bash
    validations:
      required: true
  - type: textarea
    id: expected
    attributes:
      label: "Ожидаемое поведение"
      description: "Что должно было произойти?"
      placeholder: "При нажатии на кнопку X должно происходить Z"
    validations:
      required: true
  - type: textarea
    id: additional
    attributes:
      label: "Дополнительная информация"
      description: "Скриншоты, логи, контекст"
      placeholder: "Приложите любую дополнительную информацию"
      required: false
